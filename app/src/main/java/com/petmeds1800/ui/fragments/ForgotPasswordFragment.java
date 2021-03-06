package com.petmeds1800.ui.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.text.Html;
import android.text.Spanned;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import javax.inject.Inject;

import com.petmeds1800.PetMedsApplication;
import com.petmeds1800.R;
import com.petmeds1800.api.PetMedsApiService;
import com.petmeds1800.model.entities.ForgotPasswordRequest;
import com.petmeds1800.model.entities.ForgotPasswordResponse;
import com.petmeds1800.model.entities.SessionConfNumberResponse;
import com.petmeds1800.mvp.ForgotPasswordTask.ForgotPasswordContract;
import com.petmeds1800.util.AnalyticsUtil;
import com.petmeds1800.util.GeneralPreferencesHelper;
import com.petmeds1800.util.GetSessionCookiesHack;
import com.petmeds1800.util.Log;
import com.petmeds1800.util.RetrofitErrorHandler;
import com.petmeds1800.util.Utils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

import static com.petmeds1800.mvp.BasePresenter.API_SUCCESS_CODE;

/**
 * Created by Digvijay on 8/22/2016.
 */
public class ForgotPasswordFragment extends AbstractFragment implements ForgotPasswordContract.View, EditText.OnEditorActionListener {

    @BindView(R.id.progress_bar)
    ProgressBar mProgressBar;

    @BindView(R.id.container_forgot_password)
    FrameLayout mContainerLayout;

    @BindView(R.id.email_input)
    TextInputLayout mEmailInput;

    @BindView(R.id.email_edit)
    EditText mEmailText;

    @BindView(R.id.email_my_password_button)
    Button mEmailPasswordButton;

    @Inject
    PetMedsApiService mApiService;

    @Inject
    GeneralPreferencesHelper mPreferencesHelper;

    private ForgotPasswordContract.Presenter mPresenter;

    private GetSessionCookiesHack mGetSessionCookiesHack;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PetMedsApplication.getAppComponent().inject(this);
        new AnalyticsUtil().trackScreen(getString(R.string.label_forgot_password_analytics_title));
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_forgot_password, container, false);
        ButterKnife.bind(this, view);
        mEmailText.setImeOptions(EditorInfo.IME_ACTION_GO);
        mEmailText.setOnEditorActionListener(this);
        return view;
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
                } else {
                    makeForgotPasswordApiCall();
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
        //TODO: add error icon if needed according to design or apply set error icon on edittext
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
    public void setPresenter(ForgotPasswordContract.Presenter presenter) {
        mPresenter = presenter;
    }

    @OnClick(R.id.email_my_password_button)
    public void sendForgotPasswordEmail() {
        mEmailInput.setError(null);
        String emailText = mEmailText.getText().toString().trim();

        if (emailText.isEmpty()) {
            setEmailError(getString(R.string.accountSettingsEmailEmptyError));
            return;
        } else if (!mPresenter.validateEmail(emailText)) {
            setEmailError(getString(R.string.accountSettingsEmailInvalidError));
            return;
        }

        mGetSessionCookiesHack.doHackForGettingSessionCookies(false, mApiService);
    }

    private void makeForgotPasswordApiCall() {
        showProgress();
        mApiService.getSessionConfirmationNumber()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .onErrorReturn(new Func1<Throwable, SessionConfNumberResponse>() {
                    @Override
                    public SessionConfNumberResponse call(Throwable throwable) {
                        int errorId = RetrofitErrorHandler.getErrorMessage(throwable);
                        if (errorId == R.string.noInternetConnection) {
                            hideProgress();
                            showErrorCrouton(getString(errorId), false);
                        } else {
                            return mPreferencesHelper.getSessionConfirmationResponse();
                        }
                        return null;
                    }
                })
                .flatMap(new ForgotPasswordObservable())
                .subscribe(new Subscriber<ForgotPasswordResponse>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        hideProgress();
                        int errorId = RetrofitErrorHandler.getErrorMessage(e);
                        if (errorId == R.string.noInternetConnection) {
                            showErrorCrouton(getString(errorId), false);
                        }
                        Log.v("onError", e.getMessage());
                    }

                    @Override
                    public void onNext(ForgotPasswordResponse response) {
                        hideProgress();
                        if (response != null) {
                            Log.v("response", response.getStatus().getCode());
                            if (response.getStatus().getCode().equals(API_SUCCESS_CODE)) {
                                Toast.makeText(getContext(), getContext().getString(R.string.password_sent_success_message), Toast.LENGTH_LONG).show();
                            } else {
                                showErrorCrouton(Html.fromHtml(response.getStatus().getErrorMessages().get(0)),
                                        true);
                            }
                        }
                    }
                });

    }

    class ForgotPasswordObservable implements Func1<SessionConfNumberResponse, Observable<ForgotPasswordResponse>> {
        @Override
        public Observable<ForgotPasswordResponse> call(
                SessionConfNumberResponse sessionConfNumberResponse) {
            if (sessionConfNumberResponse != null) {
                String sessionConfNumber = sessionConfNumberResponse.getSessionConfirmationNumber();
                Log.v("sessionToken", sessionConfNumber);
                if (sessionConfNumber != null) {
                    mPreferencesHelper.saveSessionConfirmationResponse(sessionConfNumberResponse);
                }

                return mApiService
                        .forgotPassword(
                                new ForgotPasswordRequest(mEmailText.getText().toString().trim(), sessionConfNumber))
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.io());
            } else {
                return null;
            }
        }
    }

    @Override
    public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
        if (actionId == EditorInfo.IME_ACTION_GO) {
            sendForgotPasswordEmail();
        }
        return false;
    }
}
