package blogtalk.com.socialmedia;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import blogtalk.com.apiservices.APIClient;
import blogtalk.com.apiservices.APIInterface;
import blogtalk.com.apiservices.RespUserList;
import blogtalk.com.chat.ChatHelper;
import blogtalk.com.utils.Constants;
import blogtalk.com.utils.Methods;
import blogtalk.com.utils.SharedPref;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.onesignal.OneSignal;

import cn.refactor.library.SmoothCheckBox;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private String from = "";

    SharedPref sharedPref;
    EditText editText_email, editText_password;
    Button button_login, button_skip;
    TextView tv_forgotpass, tv_remember, tv_terms, tv_register;
    Methods methods;
    ProgressDialog progressDialog;
    SmoothCheckBox cb_rememberme, checkbox_terms;
    private MaterialCardView btn_login_google;
    private FirebaseAuth mAuth;

    APIInterface apiInterface;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        apiInterface = APIClient.getClient().create(APIInterface.class);

        mAuth = FirebaseAuth.getInstance();

//        from = getIntent().getStringExtra("from");

        sharedPref = new SharedPref(this);
        sharedPref.setIsLoginShown(true);
        methods = new Methods(this);
        methods.forceRTLIfSupported();

        progressDialog = new ProgressDialog(LoginActivity.this);
        progressDialog.setMessage(getString(R.string.loading));
        progressDialog.setCancelable(false);

        btn_login_google = findViewById(R.id.cv_google);

        cb_rememberme = findViewById(R.id.checkbox_login);
        checkbox_terms = findViewById(R.id.checkbox_terms_login);
        editText_email = findViewById(R.id.et_login_email);
        editText_password = findViewById(R.id.et_login_password);
        button_login = findViewById(R.id.button_login);
        button_skip = findViewById(R.id.button_skip);
        tv_remember = findViewById(R.id.tv_login_remember_me);
        tv_register = findViewById(R.id.tv_login_register);
        tv_forgotpass = findViewById(R.id.tv_forgotpass);
        tv_terms = findViewById(R.id.tv_terms_login);

        if (sharedPref.getIsRemember()) {
            editText_email.setText(sharedPref.getEmail());
            editText_password.setText(sharedPref.getPassword());
        }

        tv_remember.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cb_rememberme.setChecked(!cb_rememberme.isChecked(), true);
            }
        });

        findViewById(R.id.tv3).setOnClickListener(view -> {
            checkbox_terms.setChecked(!checkbox_terms.isChecked(), true);
        });

        button_skip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                methods.setOnesignalIDs("", "");
                openMainActivity();
            }
        });

        tv_register.setOnClickListener(view -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        tv_forgotpass.setOnClickListener(view -> {
            Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
            startActivity(intent);
        });
        tv_terms.setOnClickListener(view -> {
            for (int i = 0; i < Constants.itemAbout.getArrayListPages().size(); i++) {
                if (Constants.itemAbout.getArrayListPages().get(i).getId().equals("2")) {
                    Intent intent = new Intent(LoginActivity.this, WebviewActivity.class);
                    intent.putExtra("item", Constants.itemAbout.getArrayListPages().get(i));
                    startActivity(intent);
                }
            }
        });

        button_login.setOnClickListener(view -> attemptLogin());

        btn_login_google.setOnClickListener(view -> {
            if (checkbox_terms.isChecked()) {
                if (methods.isNetworkAvailable()) {
                    GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                            .requestIdToken(getString(R.string.default_web_client_id))
                            .requestEmail()
                            .build();

                    GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(LoginActivity.this, gso);

                    Intent signInIntent = mGoogleSignInClient.getSignInIntent();
//                    startActivityForResult(signInIntent, 112);
                    activityLauncher.launch(signInIntent);
                } else {
                    Toast.makeText(LoginActivity.this, getString(R.string.err_internet_not_connected), Toast.LENGTH_SHORT).show();
                }
            } else {
                methods.showToast(getString(R.string.agree_terms));
            }
        });
    }

    private void attemptLogin() {
        editText_email.setError(null);
        editText_password.setError(null);

        // Store values at the time of the login attempt.
        String email = editText_email.getText().toString();
        String password = editText_password.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            editText_password.setError(getString(R.string.error_password_sort));
            focusView = editText_password;
            cancel = true;
        }
        if (editText_password.getText().toString().endsWith(" ")) {
            editText_password.setError(getString(R.string.pass_end_space));
            focusView = editText_password;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            editText_email.setError(getString(R.string.cannot_empty));
            focusView = editText_email;
            cancel = true;
        } else if (!isEmailValid(email)) {
            editText_email.setError(getString(R.string.err_invalid_email));
            focusView = editText_email;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {

            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(editText_email.getWindowToken(), 0);
            imm.hideSoftInputFromWindow(editText_password.getWindowToken(), 0);

            editText_email.clearFocus();
            editText_password.clearFocus();


            loadLogin();
        }
    }

    private void loadLogin() {
        if (methods.isNetworkAvailable()) {

            progressDialog.show();

            Call<RespUserList> call = apiInterface.getLogin(methods.getAPIRequest(Constants.URL_LOGIN, "", "", "", "", "", "", "", editText_email.getText().toString(), editText_password.getText().toString(), "", "", ""));
            call.enqueue(new Callback<RespUserList>() {
                @Override
                public void onResponse(@NonNull Call<RespUserList> call, @NonNull Response<RespUserList> response) {
                    if (response.body() != null) {
                        if (response.body().getSuccess().equals("1")) {
                            if (response.body().getUserDetail() != null) {

                                OneSignal.login(response.body().getUserDetail().getId());
                                methods.setOnesignalIDs(OneSignal.getUser().getPushSubscription().getId(), response.body().getUserDetail().getId());

                                sharedPref.setLoginDetails(response.body().getUserDetail().getId(), response.body().getUserDetail().getName(), response.body().getUserDetail().getMobile(), editText_email.getText().toString(), response.body().getUserDetail().getImage(), "", cb_rememberme.isChecked(), editText_password.getText().toString(), Constants.LOGIN_TYPE_NORMAL, response.body().getUserDetail().getIsEmailVerified(), response.body().getUserDetail().getProfileCompleted());
                                sharedPref.setIsLogged(true);
                                sharedPref.setIsAutoLogin(true);

                                if(sharedPref.getIsChatOn()) {
                                    ChatHelper.getInstance().initSDK(LoginActivity.this);
                                }

//                            if (from.equals("app")) {
//                                finish();
//                            } else {
//                                if(response.body().getUserDetail().getProfileCompleted() == 100) {
                                    openMainActivity();
//                                } else {
//                                    Intent intent = new Intent(LoginActivity.this, ProfileEditActivity.class);
//                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                                    startActivity(intent);
//                                    finish();
//                                }
//                            }
                            }
                        }
                        methods.showToast(response.body().getMessage());
                    } else {
                        methods.showToast(getString(R.string.err_server_error));
                    }
                    progressDialog.dismiss();
                }

                @Override
                public void onFailure(@NonNull Call<RespUserList> call, @NonNull Throwable t) {
                    call.cancel();
                    methods.showToast(getString(R.string.err_server_error));
                    progressDialog.dismiss();
                }
            });
        } else {
            methods.showToast(getString(R.string.err_internet_not_connected));
        }
    }

    private boolean isEmailValid(String email) {
        return email.contains("@") && !email.contains(" ");
    }

    private boolean isPasswordValid(String password) {
        return password.length() > 0;
    }

    private void openMainActivity() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void loadLoginSocial(final String loginType, final String name, String email, final String authId) {
        if (methods.isNetworkAvailable()) {

            progressDialog.show();

            Call<RespUserList> call = apiInterface.getSocialLogin(methods.getAPIRequest(Constants.URL_SOCIAL_LOGIN, authId, loginType, "", "", "", "", name, email, "", "", "", ""));
            call.enqueue(new Callback<RespUserList>() {
                @Override
                public void onResponse(@NonNull Call<RespUserList> call, @NonNull Response<RespUserList> response) {
                    if (response.body() != null) {
                        switch (response.body().getSuccess()) {
                            case "1":
                                if (response.body().getUserDetail() != null) {
                                    sharedPref.setLoginDetails(response.body().getUserDetail().getId(), response.body().getUserDetail().getName(), "", email, response.body().getUserDetail().getImage(), authId, cb_rememberme.isChecked(), "", loginType,     response.body().getUserDetail().getIsEmailVerified(), response.body().getUserDetail().getProfileCompleted());
                                    sharedPref.setIsLogged(true);
                                    sharedPref.setIsAutoLogin(true);

                                    OneSignal.login(response.body().getUserDetail().getId());
                                    methods.setOnesignalIDs(OneSignal.getUser().getPushSubscription().getId(), response.body().getUserDetail().getId());

                                    methods.showToast(response.body().getMessage());

                                    if(sharedPref.getIsChatOn()) {
                                        ChatHelper.getInstance().initSDK(LoginActivity.this);
                                    }

//                                if (from.equals("app")) {
//                                    finish();
//                                } else {
//                                    if(response.body().getUserDetail().getProfileCompleted() == 100) {
                                        openMainActivity();
//                                    } else {
//                                        Intent intent = new Intent(LoginActivity.this, ProfileCompleteActivity.class);
//                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                                        startActivity(intent);
//                                        finish();
//                                    }
//                                }
                                } else {
                                    methods.showToast(getString(R.string.err_server_error));
                                }
                                break;
                            case "-1":
                                methods.getVerifyDialog(getString(R.string.err_unauth_access), response.body().getMessage());
                                break;
                            default:
                                if (response.body().getMessage().contains("already") || response.body().getMessage().contains("Invalid email format")) {
                                    editText_email.setError(response.body().getMessage());
                                    editText_email.requestFocus();
                                } else {
                                    Toast.makeText(LoginActivity.this, response.body().getMessage(), Toast.LENGTH_SHORT).show();
                                }

                                try {
//                                    if (loginType.equals(Constants.LOGIN_TYPE_FB)) {
//                                        LoginManager.getInstance().logOut();
//                                    } else if (loginType.equals(Constants.LOGIN_TYPE_GOOGLE)) {
                                    FirebaseAuth.getInstance().signOut();
//                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                break;
                        }
                    } else {
                        methods.showToast(getString(R.string.err_server_error));
                    }
                    progressDialog.dismiss();
                }

                @Override
                public void onFailure(@NonNull Call<RespUserList> call, @NonNull Throwable t) {
                    call.cancel();
                    methods.showToast(getString(R.string.err_server_error));
                    progressDialog.dismiss();
                }
            });
        } else {
            methods.showToast(getString(R.string.err_internet_not_connected));
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = mAuth.getCurrentUser();

                            loadLoginSocial(Constants.LOGIN_TYPE_GOOGLE, user.getDisplayName(), user.getEmail(), user.getUid());
                        } else {
                            Toast.makeText(LoginActivity.this, "Failed to Sign IN", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    ActivityResultLauncher<Intent> activityLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    try {
                            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                            firebaseAuthWithGoogle(task.getResult().getIdToken());
                    } catch (Exception e) {
                        Toast.makeText(LoginActivity.this, getString(R.string.err_login_google), Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(LoginActivity.this, getString(R.string.err_login_google), Toast.LENGTH_SHORT).show();
                }
            }
    );

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        if (requestCode == 112) {
//            // The Task returned from this call is always completed, no need to attach
//            // a listener.
//            try {
//                if (resultCode != 0) {
//                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
//                    firebaseAuthWithGoogle(task.getResult().getIdToken());
//                } else {
//                    Toast.makeText(LoginActivity.this, getString(R.string.err_login_google), Toast.LENGTH_SHORT).show();
//                }
//            } catch (Exception e) {
//                Toast.makeText(LoginActivity.this, getString(R.string.err_login_google), Toast.LENGTH_SHORT).show();
//                e.printStackTrace();
//            }
//        }
////        else {
////            callbackManager.onActivityResult(requestCode, resultCode, data);
////        }
//        super.onActivityResult(requestCode, resultCode, data);
//    }
}