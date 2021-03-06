package com.petmeds1800.ui.fragments;

import com.petmeds1800.PetMedsApplication;
import com.petmeds1800.R;
import com.petmeds1800.api.PetMedsApiService;
import com.petmeds1800.intent.CheckOutIntent;
import com.petmeds1800.intent.ForgotPasswordIntent;
import com.petmeds1800.intent.HomeIntent;
import com.petmeds1800.intent.SignUpIntent;
import com.petmeds1800.model.entities.LoginRequest;
import com.petmeds1800.model.entities.LoginResponse;
import com.petmeds1800.model.entities.SessionConfNumberResponse;
import com.petmeds1800.mvp.LoginTask.LoginContract;
import com.petmeds1800.ui.LoginActivity;
import com.petmeds1800.util.AnalyticsUtil;
import com.petmeds1800.util.GeneralPreferencesHelper;
import com.petmeds1800.util.GetSessionCookiesHack;
import com.petmeds1800.util.RetrofitErrorHandler;
import com.petmeds1800.util.Utils;
import com.urbanairship.UAirship;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.text.Html;
import android.text.Spanned;
import com.petmeds1800.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

//import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by Digvijay on 8/3/2016.
 */
public class LoginFragment extends AbstractFragment implements LoginContract.View, EditText.OnEditorActionListener {

    @BindView(R.id.progress_bar)
    ProgressBar mProgressBar;

    @BindView(R.id.email_input)
    TextInputLayout mEmailInput;

    @BindView(R.id.email_edit)
    EditText mEmailEdit;

    @BindView(R.id.password_input)
    TextInputLayout mPasswordInput;

    @BindView(R.id.password_edit)
    EditText mPasswordEdit;

    @BindView(R.id.container_login)
    RelativeLayout mContainerLayout;

    @Inject
    PetMedsApiService mApiService;

    @Inject
    GeneralPreferencesHelper mPreferencesHelper;

    private static final String IS_FROM_HOME_ACTIVITY = "isFromHomeActivity";

    private LoginContract.Presenter mPresenter;

    private GetSessionCookiesHack mGetSessionCookiesHack;

    public static LoginFragment newInstance() {
        return new LoginFragment();
    }

    public LoginFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PetMedsApplication.getAppComponent().inject(this);
        new AnalyticsUtil().trackScreen(getString(R.string.label_login_analytics_title));
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_login, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mPasswordEdit.setImeOptions(EditorInfo.IME_ACTION_GO);
        mPasswordEdit.setOnEditorActionListener(this);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //Temp Hack for login
        mGetSessionCookiesHack = new GetSessionCookiesHack() {
            @Override
            public void getSessionCookiesOnFinish(boolean doLogin, Throwable e) {
                int errorId = RetrofitErrorHandler.getErrorMessage(e);

                if (errorId == R.string.noInternetConnection) {
                    showErrorCrouton(getString(errorId), false);
                    hideProgress();
                } else if (!(e instanceof SecurityException)){
                    showErrorCrouton(e.getMessage(), false);
                    hideProgress();
                } else {
                    if (doLogin) {
                        doLogin();
                    } else if((e instanceof SecurityException)) {
//                        initializeSessionConfirmationNumber();
                        navigateToHomeOrCheckout();
                    }
                }
            }

            @Override
            public void getSessionCookiesShowProgress() {
                showProgress();
            }

            @Override
            public void getSessionCookiesHideProgress() {
                hideProgress();
            }
        };
    }

    @Override
    public void onResume() {
        super.onResume();
        mPresenter.start();
    }

    @Override
    public void showProgress() {
        mProgressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideProgress() {
        mProgressBar.setVisibility(View.INVISIBLE);
    }

    @Override
    public void setEmailError(String errorString) {
        mEmailInput.setError(errorString);
    }

    @Override
    public void setPasswordError(String errorString) {
        mPasswordInput.setError(errorString);
    }

    @Override
    public void showErrorCrouton(CharSequence message, boolean span) {
        if (span) {
            Utils.displayCrouton(getActivity(), (Spanned) message, mContainerLayout);
        } else {
            Utils.displayCrouton(getActivity(), (String) message, mContainerLayout);
        }
    }

    @Override
    public void navigateToHomeOrCheckout() {

        //check if we need to start the checkout activity and home activity in a task builder
        Intent loginIntent = getActivity().getIntent();
        if (loginIntent != null) { //check if the intent is to start the checkout flow
            if (loginIntent.getAction() != null && loginIntent.getAction().equals(LoginActivity.START_CHECKOUT)) {
                CheckOutIntent checkOutIntent = new CheckOutIntent(getActivity());
                checkOutIntent.putExtra(CartFragment.SHOPPING_CART,
                        loginIntent.getSerializableExtra(CartFragment.SHOPPING_CART));
                startActivity(checkOutIntent);
                getActivity().finishAffinity();
            } else {
                HomeIntent intent = new HomeIntent(getActivity());
                intent.putExtra(IS_FROM_HOME_ACTIVITY, true);
                startActivity(intent);
                getActivity().finish();
            }
        }
    }

    @Override
    public void setPresenter(LoginContract.Presenter presenter) {
//        mPresenter = checkNotNull(presenter);
        mPresenter = presenter;
    }

    @OnClick(R.id.log_in_button)
    public void login() {
        mEmailInput.setError(null);
        mPasswordInput.setError(null);

        String emailText = mEmailEdit.getText().toString().trim();
        if (emailText.isEmpty()) {
            setEmailError(getString(R.string.accountSettingsEmailEmptyError));
            return;
        } else if (!mPresenter.validateEmail(emailText)) {
            setEmailError(getString(R.string.accountSettingsEmailInvalidError));
            return;
        }

        String passwordText = mPasswordEdit.getText().toString().trim();
        if (passwordText.isEmpty()) {
            setPasswordError(getString(R.string.accountSettingsPasswordEmptyError));
            return;
        } else if (!mPresenter.validatePassword(passwordText)) {
            setPasswordError(getString(R.string.accountSettingsPasswordInvalidError));
            return;
        }

        doLogin();
    }

    private void doLogin() {

        final String emailText = mEmailEdit.getText().toString().trim();
        final String passwordText = mPasswordEdit.getText().toString().trim();

        showProgress();

        String sessionConfirmNumber;

        if(mPreferencesHelper.getSessionConfirmationResponse() == null) {
            sessionConfirmNumber = "";
        }
        else {
            sessionConfirmNumber = mPreferencesHelper.getSessionConfirmationResponse().getSessionConfirmationNumber();
        }

        mApiService
                .login(new LoginRequest(emailText, passwordText, sessionConfirmNumber))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Subscriber<LoginResponse>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        hideProgress();
                        if (e instanceof SecurityException) {
                            doLogin();
                        } else {
                            int errorId = RetrofitErrorHandler.getErrorMessage(e);
                            if (errorId == R.string.noInternetConnection) {
                                showErrorCrouton(getString(errorId), false);
                            } else {
                                showErrorCrouton(e.getLocalizedMessage(), false);
                            }
                        }

                        Log.v("onError", e.getMessage());
                    }

                    @Override
                    public void onNext(LoginResponse loginResponse) {
                        hideProgress();
                        if (loginResponse != null) {
                            Log.v("login response", loginResponse.getStatus().getCode());
                            if (loginResponse.getStatus().getCode().equals("SUCCESS")) {
                                Log.v("login response", loginResponse.getProfile().getUserId());
                                UAirship.shared().getNamedUser().setId(loginResponse.getProfile().getUserId());
                                mPreferencesHelper.setIsUserLoggedIn(true);
                                mPreferencesHelper.setLoginEmail(loginResponse.getProfile().getEmail());
                                mPreferencesHelper.setLoginPassword(passwordText);
                                navigateToHomeOrCheckout();
                            } else {
                                showErrorCrouton(Html.fromHtml(loginResponse.getStatus().getErrorMessages().get(0)),
                                        true);
                            }
                        }
                    }
                });
    }

    @OnClick(R.id.label_skip)
    public void skipLoginSignUp() {
        SessionConfNumberResponse sessionConfNumberResponse = mPreferencesHelper.getSessionConfirmationResponse();
        if (sessionConfNumberResponse != null && sessionConfNumberResponse.getSessionConfirmationNumber() != null) {
            navigateToHomeOrCheckout();
        } else {
            showProgress();
            mGetSessionCookiesHack.doHackForGettingSessionCookies(false, mApiService);
        }

    }

    @OnClick(R.id.label_forgot_password)
    public void forgotPassword() {
        startActivity(new ForgotPasswordIntent(getActivity()));
    }

    @OnClick(R.id.sign_up_button)
    public void signUp() {
        startActivity(new SignUpIntent(getActivity()));
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_GO) {
            login();
        }
        return false;
    }
}
