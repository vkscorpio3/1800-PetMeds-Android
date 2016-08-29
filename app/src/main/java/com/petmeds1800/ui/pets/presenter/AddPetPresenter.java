package com.petmeds1800.ui.pets.presenter;

import android.support.annotation.NonNull;
import android.util.Log;

import com.petmeds1800.PetMedsApplication;
import com.petmeds1800.api.PetMedsApiService;
import com.petmeds1800.model.entities.AddPetRequest;
import com.petmeds1800.model.entities.AddPetResponse;
import com.petmeds1800.ui.pets.support.AddPetContract;

import javax.inject.Inject;

import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by pooja on 8/26/2016.
 */
public class AddPetPresenter implements AddPetContract.Presenter {
    @Inject
    PetMedsApiService mPetMedsApiService;

    private AddPetContract.View mView;

    public AddPetPresenter(@NonNull AddPetContract.View view) {
        mView = view;
        mView.setPresenter(this);
        PetMedsApplication.getAppComponent().inject(this);

    }


    @Override
    public void addPetData(AddPetRequest addPetRequest) {
        mPetMedsApiService.addPet(addPetRequest)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<AddPetResponse>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        //error handling would be implemented once we get the details from backend team
                        mView.onError(e.getLocalizedMessage());

                    }

                    @Override
                    public void onNext(AddPetResponse s) {
                        Log.d("Addpetresponse",s.toString());
                        if(s.getStatus().getCode().equals(API_SUCCESS_CODE)) {
                       if(mView.isActive()){
                           mView.onSuccess();
                       }
                        }else{
                            if(mView.isActive()){
                                mView.onError(s.getStatus().getErrorMessages().get(0));
                            }
                        }

                    }
                });
    }



    @Override
    public void start() {


    }

}
