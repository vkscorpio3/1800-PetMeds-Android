package com.petmeds1800.ui.address;

import android.util.Log;

import com.petmeds1800.PetMedsApplication;
import com.petmeds1800.api.PetMedsApiService;
import com.petmeds1800.model.Country;
import com.petmeds1800.model.CountryListResponse;
import com.petmeds1800.model.StatesListResponse;
import com.petmeds1800.model.UsaState;
import com.petmeds1800.model.entities.AddAddressResponse;
import com.petmeds1800.model.entities.AddressRequest;

import java.util.HashMap;

import javax.inject.Inject;

import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by Abhinav on 13/8/16.
 */
public class AddAddressPresenter implements AddEditAddressContract.Presenter {

    @Inject
    PetMedsApiService mPetMedsApiService;

    private final AddEditAddressContract.View mView;
    private HashMap<String , String> usaStatedHashMap;
    private HashMap<String, String> countryHashMap;

    AddAddressPresenter(AddEditAddressContract.View view){
        mView = view;
        mView.setPresenter(this);
        PetMedsApplication.getAppComponent().inject(this);
    }

    @Override
    public void saveAddress(AddressRequest addressRequest) {
        //show the progress
        mPetMedsApiService.addAddress(addressRequest)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<AddAddressResponse>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        //error handling would be implemented once we get the details from backend team
                        Log.e("AddACard", e.getMessage());
                        mView.addressAdditionFailed(e.getLocalizedMessage());
                    }

                    @Override
                    public void onNext(AddAddressResponse s) {
                        if (s.getStatus().getCode().equals(API_SUCCESS_CODE)) {
                            if (mView.isActive()) {
                                mView.addressAdded();
                            }
                        } else {
                            Log.d("AddACard", s.getStatus().getErrorMessages().get(0));
                            if (mView.isActive()) {
                                mView.addressAdditionFailed(s.getStatus().getErrorMessages().get(0));
                            }
                        }

                    }
                });

    }

    @Override
    public void getUsaStatesList() {
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
                        Log.e("AddACard",e.getMessage());
                        mView.addressAdditionFailed(e.getLocalizedMessage());
                    }

                    @Override
                    public void onNext(StatesListResponse s) {
                        if(s.getStatus().getCode().equals(API_SUCCESS_CODE)){
                            if(mView.isActive()){
                                //StateName would be treated as key while stateCode would be treated as a key so that we could get the code as user selects one name
                                usaStatedHashMap = new HashMap<>(s.getStateList().size());
                                for(UsaState eachUsaState : s.getStateList()) {
                                    usaStatedHashMap.put(eachUsaState.getDisplayName(),eachUsaState.getCode());
                                }
                                mView.usaStatesListReceived(usaStatedHashMap.keySet().toArray(new String[usaStatedHashMap.size()]));
                            }
                        }
                        else{
                            Log.d("AddACard",s.getStatus().getErrorMessages().get(0));
                            if(mView.isActive()){
                                mView.addressAdditionFailed(s.getStatus().getErrorMessages().get(0));
                            }
                        }

                    }
                });
    }

    @Override
    public String getUsaStateCode(String usaStateName) {
        if(usaStatedHashMap != null) {
            return usaStatedHashMap.get(usaStateName);
        }
        return null;
    }

    @Override
    public void getCountryList() {
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
                        Log.e("AddACard",e.getMessage());
                        mView.addressAdditionFailed(e.getLocalizedMessage());
                    }

                    @Override
                    public void onNext(CountryListResponse s) {
                        if(s.getStatus().getCode().equals(API_SUCCESS_CODE)){
                            if(mView.isActive()){
                                //StateName would be treated as key while stateCode would be treated as a key so that we could get the code as user selects one name
                                countryHashMap = new HashMap<>(s.getCountryList().size());
                                for(Country eachCountry : s.getCountryList()) {
                                    countryHashMap.put(eachCountry.getDisplayName(), eachCountry.getCode());
                                }
                                mView.countryListReceived(countryHashMap.keySet().toArray(new String[countryHashMap.size()]));
                            }
                        }
                        else{
                            Log.d("AddACard",s.getStatus().getErrorMessages().get(0));
                            if(mView.isActive()){
                                mView.addressAdditionFailed(s.getStatus().getErrorMessages().get(0));
                            }
                        }

                    }
                });
    }

    @Override
    public String getCountryCode(String countryName) {
        if(countryHashMap != null) {
            return countryHashMap.get(countryName);
        }
        return null;
    }

    @Override
    public void updateAddress(AddressRequest addressRequest) {
        //show the progress
        mPetMedsApiService.updateAddress(addressRequest)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<AddAddressResponse>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        //error handling would be implemented once we get the details from backend team
                        Log.e("AddACard", e.getMessage());
                        mView.addressAdditionFailed(e.getLocalizedMessage());
                    }

                    @Override
                    public void onNext(AddAddressResponse s) {
                        if (s.getStatus().getCode().equals(API_SUCCESS_CODE)) {
                            if (mView.isActive()) {
                                mView.addressAdded();
                            }
                        } else {
                            Log.d("AddACard", s.getStatus().getErrorMessages().get(0));
                            if (mView.isActive()) {
                                mView.addressAdditionFailed(s.getStatus().getErrorMessages().get(0));
                            }
                        }

                    }
                });
    }

    @Override
    public void start() {

    }
}
