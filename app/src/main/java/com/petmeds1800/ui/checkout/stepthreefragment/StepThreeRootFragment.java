package com.petmeds1800.ui.checkout.stepthreefragment;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;

import com.petmeds1800.PetMedsApplication;
import com.petmeds1800.R;
import com.petmeds1800.intent.AddNewEntityIntent;
import com.petmeds1800.model.Address;
import com.petmeds1800.model.Card;
import com.petmeds1800.model.PayPalCheckoutRequest;
import com.petmeds1800.model.entities.CreditCardPaymentMethodRequest;
import com.petmeds1800.model.shoppingcart.response.ShippingAddress;
import com.petmeds1800.model.shoppingcart.response.ShippingGroups;
import com.petmeds1800.model.shoppingcart.response.ShoppingCartListResponse;
import com.petmeds1800.ui.address.AddEditAddressFragment;
import com.petmeds1800.ui.checkout.CheckOutActivity;
import com.petmeds1800.ui.checkout.CommunicationFragment;
import com.petmeds1800.ui.fragments.AbstractFragment;
import com.petmeds1800.ui.fragments.CartFragment;
import com.petmeds1800.ui.fragments.CommonWebviewFragment;
import com.petmeds1800.util.AnalyticsUtil;
import com.petmeds1800.util.Constants;
import com.petmeds1800.util.GeneralPreferencesHelper;
import com.petmeds1800.util.Log;
import com.petmeds1800.util.Utils;

import java.util.ArrayList;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Sdixit on 27-09-2016.
 */

public class StepThreeRootFragment extends AbstractFragment
        implements StepThreeRootContract.View, CheckOutActivity.PaypalErrorListener {


    @BindView(R.id.newPaymentMethod)
    Button mNewPaymentMethod;

    public final static int LOGGED_IN_REQUEST_CODE = 6;

    public final static int GUEST_REQUEST_CODE = 12;

    private static String REQUEST_CODE_KEY = "request_code";

    private static final String REVIEW_ON_KEY = "review_on";

    @BindView(R.id.shippingNavigator)
    Button mShippingNavigator;

    @BindView(R.id.stepThreeContainerLayout)
    RelativeLayout mContainerLayout;

    private ShoppingCartListResponse mShoppingCartListResponse;

    private Address mAddress;

    private CheckOutActivity activity;

    private Bundle mBundle;

    private String mStepName;

    private StepThreeRootContract.Presenter mPresenter;

    private Card mCard;

    @Inject
    GeneralPreferencesHelper mPreferencesHelper;

    @BindView(R.id.payPalCheckBox)
    CheckBox mPaypalCheckbox;

    private int mRequestCode;

    private boolean mReviewOn;

    @BindView(R.id.creditCardCheckBox)
    CheckBox mCreditCardCheckbox;




    public static StepThreeRootFragment newInstance(ShoppingCartListResponse shoppingCartListResponse,
                                                    String stepName, int requestCode) {
        StepThreeRootFragment f = new StepThreeRootFragment();
        Bundle args = new Bundle();
        args.putSerializable(CartFragment.SHOPPING_CART, shoppingCartListResponse);
        args.putString(CheckOutActivity.STEP_NAME, stepName);
        args.putInt(REQUEST_CODE_KEY, requestCode);
        f.setArguments(args);
        return f;
    }

    public static StepThreeRootFragment newInstance(ShoppingCartListResponse shoppingCartListResponse,
                                                    String stepName, int requestCode, boolean mReviewIsOn) {
        StepThreeRootFragment f = new StepThreeRootFragment();
        Bundle args = new Bundle();
        args.putSerializable(CartFragment.SHOPPING_CART, shoppingCartListResponse);
        args.putString(CheckOutActivity.STEP_NAME, stepName);
        args.putInt(REQUEST_CODE_KEY, requestCode);
        args.putBoolean(REVIEW_ON_KEY, mReviewIsOn);
        f.setArguments(args);
        return f;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);


    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new AnalyticsUtil().trackScreen(getString(R.string.label_payment_method_analytics_title));
        mShoppingCartListResponse = (ShoppingCartListResponse) getArguments()
                .getSerializable(CartFragment.SHOPPING_CART);
        mStepName = getArguments().getString(CheckOutActivity.STEP_NAME);

        Bundle bundle = getArguments();
        if (bundle != null) {
            mRequestCode = bundle.getInt(REQUEST_CODE_KEY);
            mReviewOn = bundle.getBoolean(REVIEW_ON_KEY);
        }
        ((CheckOutActivity)getActivity()).addListener(this);

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_step_three_checkout, container, false);
        PetMedsApplication.getAppComponent().inject(this);
        ButterKnife.bind(this, view);
        if (((CheckOutActivity) getActivity()).getApplicableSteps() == 4) {
            mShippingNavigator.setText(getString(R.string.review_submit_navigator_button_title));
        }
        populateAddress();
       /* mPaypalCheckbox.setOnCheckedChangeListener(this);
        mCreditCardCheckbox.setOnCheckedChangeListener(this);*/
        Log.d("oncreateview", "STEPTHREEROOTFRAGMENT");

        mPaypalCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(buttonView.isPressed()) {
                    if (mCreditCardCheckbox.isChecked()) {
                        mCreditCardCheckbox.setChecked(false);
                    }

                    if (mPaypalCheckbox.isChecked()) {
                        activity.showProgress();
                        PayPalCheckoutRequest payPalCheckoutRequest = new PayPalCheckoutRequest("checkout");
                        mPresenter.checkoutPayPal(payPalCheckoutRequest);
                    }
                }
            }
        });
        mCreditCardCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (buttonView.isPressed()) {
                    if (mPaypalCheckbox.isChecked()) {
                        mPaypalCheckbox.setChecked(false);
                    }
                }


            }
        });

        return view;

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        activity = (CheckOutActivity) getActivity();
        if (savedInstanceState == null) {
            activity.setToolBarTitle(getString(R.string.payment_method_header));
            activity.setLastCompletedSteps(mStepName);
            activity.setActiveStep(mStepName);
        }

    }


    public Card getCard() {
        return mCard;
    }

    public void setCard(Card card) {
        mCard = card;

    }

    public void populateAddress() {

        ArrayList<ShippingGroups> shippingGroupses = mShoppingCartListResponse.getShoppingCart().getShippingGroups();
        ShippingAddress shippingAddress = null;
        mAddress = new Address();
        if (shippingGroupses.size() > 0) {
            shippingAddress = shippingGroupses.get(0).getShippingAddress();
            mAddress.setAddress1(shippingAddress.getAddress1());
            mAddress.setAddress2(shippingAddress.getAddress2());
            mAddress.setCity(shippingAddress.getCity());
            mAddress.setState(shippingAddress.getState());
            mAddress.setCountry(shippingAddress.getCountry());
            mAddress.setFirstName(shippingAddress.getFirstName());
            mAddress.setLastName(shippingAddress.getLastName());
            mAddress.setPhoneNumber(shippingAddress.getPhoneNumber());
            mAddress.setPostalCode(shippingAddress.getPostalCode());
        }


    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mPresenter = new StepThreeRootPresentor(this);


       // Log.d("mShoppingCartListResponse",mShoppingCartListResponse.getShoppingCart().getPaymentGroups().get(0).getPaymentMethod());

        if (mShoppingCartListResponse.getShoppingCart().getPaymentGroups()!=null && mShoppingCartListResponse.getShoppingCart().getPaymentGroups().size()>0 && mShoppingCartListResponse.getShoppingCart().getPaymentGroups().get(0).getPaymentMethod()!=null && mShoppingCartListResponse.getShoppingCart().getPaymentGroups().get(0).getPaymentMethod().equalsIgnoreCase("PayPal")){
            mPaypalCheckbox.setChecked(true);
            mCreditCardCheckbox.setChecked(false);
        }else {
            mCreditCardCheckbox.setChecked(true);
            mPaypalCheckbox.setChecked(false);
        }

        if (mRequestCode == LOGGED_IN_REQUEST_CODE) {
            replaceStepRootChildFragment(
                    PaymentSelectionListFragment.newInstance(LOGGED_IN_REQUEST_CODE),
                    R.id.creditCardDetailFragment);
        } else if (mRequestCode == GUEST_REQUEST_CODE && mReviewOn) {
            replaceStepRootChildFragment(
                    PaymentSelectionListFragment.newInstance(GUEST_REQUEST_CODE,
                            mShoppingCartListResponse.getShoppingCart().getPaymentCardKey(), mReviewOn),
                    R.id.creditCardDetailFragment);
        } else {
            //qwe can ignore it as first time filling of payment for an anonymous is being handled by the GuestStepThreeFragment
        }

        replaceStepRootChildFragment(
                AddEditAddressFragment.newInstance(mAddress, StepThreeRootFragment.LOGGED_IN_REQUEST_CODE),
                R.id.billingAddressfragment);
        replaceStepRootChildFragment(CommunicationFragment.newInstance(CommunicationFragment.REQUEST_CODE_VALUE),
                R.id.communicationfragment);

    }

    @OnClick(R.id.newPaymentMethod)
    public void onClick() {
        startActivity(new AddNewEntityIntent(getActivity(), Constants.ADD_NEW_PAYMENT_METHOD));

    }

    @Override
    public boolean isActive() {
        return isAdded();
    }

    @Override
    public void onSuccessCreditCardPayment(ShoppingCartListResponse response) {
        activity.hideProgress();
        activity.moveToNext(mStepName, response);

    }

    @Override
    public void onError(String errorMessage) {
        activity.hideProgress();
    }

    @Override
    public void showErrorCrouton(CharSequence message, boolean span) {
        activity.hideProgress();
        Utils.displayCrouton(getActivity(), message.toString(), mContainerLayout);
    }

    @Override
    public void setUpdatedAddressOnSuccess(Address address) {
        activity.hideProgress();
        mAddress = address;
        ((AddEditAddressFragment) getChildFragmentManager().findFragmentById(R.id.billingAddressfragment))
                .populateData(mAddress);
    }

    @Override
    public void errorOnUpdateAddress() {
        activity.hideProgress();
    }

    @Override
    public void onSuccess(String url) {
        //  mProgressBar.setVisibility(View.GONE);
        activity.hideProgress();
        Bundle bundle = new Bundle();
        bundle.putString(CommonWebviewFragment.PAYPAL_DATA, url);
        bundle.putBoolean(CommonWebviewFragment.ISCHECKOUT, true);
        bundle.putString(CommonWebviewFragment.STEPNAME, mStepName);
        ((CheckOutActivity) getActivity())
                .replaceCheckOutFragmentWithBundle(new CommonWebviewFragment(), CommonWebviewFragment.class.getName(),
                        false, bundle);

    }

    @Override
    public void onPayPalError(String errorMsg) {
        activity.hideProgress();
        if (errorMsg.isEmpty()) {
            Utils.displayCrouton(getActivity(), getString(R.string.unexpected_error_label), mContainerLayout);

        } else {
            Utils.displayCrouton(getActivity(), errorMsg, mContainerLayout);

        }
    }

    @Override
    public void setPresenter(StepThreeRootContract.Presenter presenter) {

    }

    public void getBillingAddressId() {
        activity.showProgress();
        Card card;
        card = getCard();
        if (card != null) {
            mPresenter.getBillingAddressById(
                    mPreferencesHelper.getSessionConfirmationResponse().getSessionConfirmationNumber(),
                    card.getBillingAddress().getRepositoryId());

        }
    }

    @OnClick(R.id.shippingNavigator)
    public void onShippingNavigatorClick() {
        if(mPaypalCheckbox.isChecked()){
            activity.moveToNext(mStepName, mShoppingCartListResponse);
        }else {
            activity.showProgress();
            Card card;
            card = getCard();
            if (card != null) {
                CreditCardPaymentMethodRequest request = new CreditCardPaymentMethodRequest(card.getId()
                        , card.getBillingAddress().getRepositoryId()
                        , mPreferencesHelper.getSessionConfirmationResponse().getSessionConfirmationNumber());
                mPresenter.applyCreditCardPaymentMethod(request);
            } else {
                activity.hideProgress();
                showErrorCrouton(getString(R.string.noSavedCardsAvailableMessage), false);
            }
        }
    }

   /* @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch(buttonView.getId()){
            case R.id.payPalCheckBox:

                break;
            case R.id.creditCardCheckBox:

                break;
        }

    }
*/

    @Override
    public void onPayPal( final String errorMsg) {


        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                }

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Utils.displayCrouton(getActivity(), errorMsg, mContainerLayout);
                    }
                });
            }
        };
        thread.start();
            }

private void disableCardDetails(){

}
}
