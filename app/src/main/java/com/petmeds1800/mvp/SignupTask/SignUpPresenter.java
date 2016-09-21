package com.petmeds1800.mvp.SignupTask;

import com.petmeds1800.api.PetMedsApiService;
import com.petmeds1800.model.Country;
import com.petmeds1800.model.CountryListResponse;
import com.petmeds1800.model.StatesListResponse;
import com.petmeds1800.model.UsaState;
import com.petmeds1800.util.InputValidationUtil;
import com.petmeds1800.util.RetrofitErrorHandler;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;

import java.util.HashMap;

import javax.inject.Inject;

import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by Digvijay on 9/9/2016.
 */
public class SignUpPresenter implements SignUpContract.Presenter {

    @Inject
    PetMedsApiService mPetMedsApiService;

    private static final int PASSWORD_LENGTH = 8;

    private HashMap<String, String> statesHashMap;

    private HashMap<String, String> countriesHashMap;

    @NonNull
    private final SignUpContract.View mSignUpView;

    @Inject
    public SignUpPresenter(@NonNull SignUpContract.View signUpView) {
        mSignUpView = signUpView;
    }

    @Inject
    void setupListener() {
        mSignUpView.setPresenter(this);
    }

    @Override
    public boolean validateEmail(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    @Override
    public boolean validatePassword(String password) {
        return password.matches(InputValidationUtil.passwordPattern);
    }

    @Override
    public boolean validateUserName(String name) {
        return true;
    }

    @Override
    public boolean validateAddress(String address) {
        return true;
    }

    @Override
    public boolean validateCity(String city) {
        return true;
    }

    @Override
    public boolean validatePostalCode(String postalCode) {
        return true;
    }

    @Override
    public boolean validatePhoneNumber(String phoneNumber) {
        return true;
    }

    @Override
    public void getStatesList() {
        mSignUpView.showProgress();
        mPetMedsApiService.getUsaStatesList()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<StatesListResponse>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        //error handling would be implemented once we get the details from backend team
                        mSignUpView.hideProgress();
                        Log.e("GetStatesList", e.getMessage());
                        int errorId = RetrofitErrorHandler.getErrorMessage(e);
                        if (errorId != 0) { //internet connection error. Unknownhost or SocketTimeout exception
                            mSignUpView.showErrorCrouton(((Fragment) mSignUpView).getString(errorId), false);
                        }
                    }

                    @Override
                    public void onNext(StatesListResponse statesListResponse) {
                        mSignUpView.hideProgress();
                        if (statesListResponse.getStatus().getCode().equals(API_SUCCESS_CODE)) {
                            //StateName would be treated as key while stateCode would be treated as a key so that we could get the code as user selects one name
                            statesHashMap = new HashMap<>(statesListResponse.getStateList().size());
                            for (UsaState eachUsaState : statesListResponse.getStateList()) {
                                statesHashMap.put(eachUsaState.getDisplayName(), eachUsaState.getCode());
                            }
                            mSignUpView.onStatesListReceived(
                                    statesHashMap.keySet().toArray(new String[statesHashMap.size()]));
                        } else if (statesListResponse.getStatus().getCode().equals(API_ERROR_CODE)) {
                            Log.d("GetStatesList", statesListResponse.getStatus().getErrorMessages().get(0));
                            mSignUpView
                                    .showErrorCrouton(statesListResponse.getStatus().getErrorMessages().get(0), false);
                        }
                    }
                });
    }

    @Override
    public String getStateCode(String stateName) {
        if (statesHashMap != null) {
            return statesHashMap.get(stateName);
        }
        return null;
    }

    @Override
    public void getCountriesList() {
        mSignUpView.showProgress();
        mPetMedsApiService.getCountryList()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<CountryListResponse>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        //error handling would be implemented once we get the details from backend team
                        mSignUpView.hideProgress();
                        Log.e("GetCountriesList", e.getMessage());
                        int errorId = RetrofitErrorHandler.getErrorMessage(e);
                        if (errorId != 0) { //internet connection error. Unknownhost or SocketTimeout exception
                            mSignUpView.showErrorCrouton(((Fragment) mSignUpView).getString(errorId), false);
                        }
                    }

                    @Override
                    public void onNext(CountryListResponse s) {
                        mSignUpView.hideProgress();
                        if (s.getStatus().getCode().equals(API_SUCCESS_CODE)) {
                            //StateName would be treated as key while stateCode would be treated as a key so that we could get the code as user selects one name
                            countriesHashMap = new HashMap<>(s.getCountryList().size());
                            for (Country eachCountry : s.getCountryList()) {
                                countriesHashMap.put(eachCountry.getDisplayName(), eachCountry.getCode());
                            }
                            mSignUpView.onCountryListReceived(
                                    countriesHashMap.keySet().toArray(new String[countriesHashMap.size()]));
                        } else {
                            Log.d("GetCountriesList", s.getStatus().getErrorMessages().get(0));
                            mSignUpView.showErrorCrouton(s.getStatus().getErrorMessages().get(0), false);
                        }

                    }
                });
    }

    @Override
    public String getCountryCode(String countryName) {
        if(countriesHashMap != null) {
            return countriesHashMap.get(countryName);
        }
        return null;
    }

    @Override
    public void start() {

    }
}