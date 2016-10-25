package com.petmeds1800.ui.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.petmeds1800.PetMedsApplication;
import com.petmeds1800.R;
import com.petmeds1800.intent.CheckOutIntent;
import com.petmeds1800.model.PayPalCheckoutRequest;
import com.petmeds1800.model.shoppingcart.request.AddItemRequestShoppingCart;
import com.petmeds1800.model.shoppingcart.request.ApplyCouponRequestShoppingCart;
import com.petmeds1800.model.shoppingcart.request.RemoveItemRequestShoppingCart;
import com.petmeds1800.model.shoppingcart.request.UpdateItemQuantityRequestShoppingCart;
import com.petmeds1800.model.shoppingcart.response.ShoppingCartListResponse;
import com.petmeds1800.ui.AbstractActivity;
import com.petmeds1800.ui.HomeActivity;
import com.petmeds1800.ui.shoppingcart.ShoppingCartListContract;
import com.petmeds1800.ui.shoppingcart.presenter.ShoppingCartListPresenter;
import com.petmeds1800.util.Constants;
import com.petmeds1800.util.GeneralPreferencesHelper;
import com.petmeds1800.util.ShoppingCartRecyclerViewAdapter;
import com.petmeds1800.util.Utils;

import java.util.HashMap;

import javax.inject.Inject;

import static com.petmeds1800.util.Utils.toggleGIFAnimantionVisibility;
import static com.petmeds1800.util.Utils.toggleProgressDialogVisibility;

/**
 * Created by pooja on 8/2/2016.
 */
public class CartFragment extends AbstractFragment implements ShoppingCartListContract.View,View.OnClickListener {

    private RecyclerView mContainerLayoutItems;
    private ShoppingCartListContract.Presenter mPresenter;
    private LinearLayout mTotalCheckOutContainer;
    private LinearLayout mItemListtContainer;
    private LinearLayout mEmptyCheckoutContainer;
    private TextInputLayout mCouponCodeLayout;
    private LinearLayout mOfferCodeContainerLayout;
    private ProgressBar mProgressBar;
    private static boolean SHOW_PROGRESSBAR_OR_ANIMATION = true;
    private static boolean HIDE_PROGRESSBAR_OR_ANIMATION = false;
    private ImageButton mPayPalCheckoutButton;


    public static int sPreviousScrollPosition = 0;
    public static final String SHOPPING_CART = "shoppingCart";

    @Inject
    GeneralPreferencesHelper mPreferencesHelper;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cart, container, false);
        PetMedsApplication.getAppComponent().inject(this);
        mContainerLayoutItems = (RecyclerView) view.findViewById(R.id.products_offered_RecyclerView);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        mContainerLayoutItems.setLayoutManager(mLayoutManager);
        mContainerLayoutItems.setItemAnimator(new DefaultItemAnimator());

        mTotalCheckOutContainer = (LinearLayout) view.findViewById(R.id.total_checkout_container);
        mItemListtContainer = (LinearLayout) view.findViewById(R.id.item_list_container);
        mEmptyCheckoutContainer = (LinearLayout) view.findViewById(R.id.order_empty_view);
        mProgressBar = (ProgressBar) view.findViewById(R.id.progressbar);
        mPayPalCheckoutButton=(ImageButton)view.findViewById(R.id.button_paypal_checkout);

        IntentFilter intentFilter=new IntentFilter();
        intentFilter.addAction(Constants.KEY_CART_FRAGMENT_INTENT_FILTER);
        intentFilter.addAction(Constants.KEY_PAYMENT_INTENT_FILTER);
        registerIntent(intentFilter,getActivity());



        mPayPalCheckoutButton.setOnClickListener(this);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        callmShoppingCartAPI(null);
        ((AbstractActivity)getActivity()).setToolBarTitle(getString(R.string.cart_title));
    }

    @Override
    protected void onReceivedBroadcast(Context context, Intent intent) {
        Log.d("action",intent.getAction());
        if (intent.getAction() == Constants.KEY_CART_FRAGMENT_INTENT_FILTER){
            callmShoppingCartAPI(null);
        }
        if(intent.getAction() == Constants.KEY_PAYMENT_INTENT_FILTER){
            Bundle bundle = intent.getExtras();
            String code=bundle.getString("code");
            if(code.equals(getString(R.string.succes_txt))) {
                ShoppingCartListResponse shoppingCartListResponse = (ShoppingCartListResponse) bundle.getSerializable("shoppingListResponse");
                Log.d("shoppingCartRepsone", shoppingCartListResponse.getShoppingCart().getTotalCommerceItemCount());
            }else{
                String errorMsg=bundle.getString("errormessage");
                Utils.displayCrouton(getActivity(), errorMsg, mItemListtContainer);
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mPresenter = null;
    }



    @Override
    public void onDestroyView() {
        super.onDestroyView();
        deregisterIntent(getActivity());
    }

    @Override
    public boolean isActive() {
        return isAdded();
    }

    @Override
    public boolean postGeneralPopulateShoppingCart(ShoppingCartListResponse shoppingCartListResponse) {

        boolean response = false;
        UpdateValueThreadResponse();
        if (null != shoppingCartListResponse && shoppingCartListResponse.getItemCount() > 0){
            response = initializeShoppingCartPage(shoppingCartListResponse);
            toggleVisibilityShoppingList(false);
        } else if (null == shoppingCartListResponse || shoppingCartListResponse.getItemCount() == 0){
            toggleVisibilityShoppingList(true);
        }

        toggleProgressDialogVisibility(HIDE_PROGRESSBAR_OR_ANIMATION, mProgressBar);
        toggleGIFAnimantionVisibility(HIDE_PROGRESSBAR_OR_ANIMATION, getActivity());

        return response;
    }

    @Override
    public boolean onError(String errorMessage, String simpleName) {

        if (simpleName.equalsIgnoreCase(ApplyCouponRequestShoppingCart.class.getSimpleName())){
            mCouponCodeLayout.setError(errorMessage);
            mOfferCodeContainerLayout.findViewById(R.id.order_status_label).setVisibility(View.GONE);
        }
        else if (simpleName.equalsIgnoreCase(UpdateItemQuantityRequestShoppingCart.class.getSimpleName())){
            Utils.displayCrouton(getActivity(), (String) errorMessage, mItemListtContainer);
        }

        toggleProgressDialogVisibility(HIDE_PROGRESSBAR_OR_ANIMATION, mProgressBar);
        toggleGIFAnimantionVisibility(HIDE_PROGRESSBAR_OR_ANIMATION, getActivity());

        return false;
    }

    @Override
    public void setPresenter(ShoppingCartListContract.Presenter presenter) { mPresenter = presenter;  }
    public void onSuccess(String url) {
        Bundle bundle = new Bundle();
        bundle.putString(CommonWebviewFragment.URL_KEY, url);
        CommonWebviewFragment webViewFragment = new CommonWebviewFragment();
        webViewFragment.setArguments(bundle);
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.cart_root_fragment_container, webViewFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }


    private View createFooter(final View footerView,ShoppingCartListResponse shoppingCartListResponse){
        mOfferCodeContainerLayout = (LinearLayout) footerView.findViewById(R.id.cart_each_item_container);
        mCouponCodeLayout = (TextInputLayout) footerView.findViewById(R.id.coupon_code_input_layout);
        Button OrderStatusLAbel = (Button) footerView.findViewById(R.id.order_status_label);

        OrderStatusLAbel.setVisibility(View.GONE);
        ((EditText) mCouponCodeLayout.getEditText()).setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER) && ((EditText) mCouponCodeLayout.getEditText()).getText() != null ) {
                    callmShoppingCartAPI(new ApplyCouponRequestShoppingCart(((EditText) mCouponCodeLayout.getEditText()).getText().toString().trim(),null));
                    return true;
                }
                return false;
            }
        });

        ((EditText) mCouponCodeLayout.getEditText()).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final int DRAWABLE_LEFT = 0;
                final int DRAWABLE_TOP = 1;
                final int DRAWABLE_RIGHT = 2;
                final int DRAWABLE_BOTTOM = 3;

                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (event.getRawX() >= (((EditText) mCouponCodeLayout.getEditText()).getRight() - ((EditText) mCouponCodeLayout.getEditText()).getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                        callmShoppingCartAPI(new ApplyCouponRequestShoppingCart(((EditText) mCouponCodeLayout.getEditText()).getText().toString().trim(), null));
                        return true;
                    }
                }
                return false;
            }
        });

        if (null != shoppingCartListResponse && null != shoppingCartListResponse.getShoppingCart().getCoupon() && !shoppingCartListResponse.getShoppingCart().getCoupon().isEmpty()){
            mCouponCodeLayout.getEditText().setText(shoppingCartListResponse.getShoppingCart().getCoupon());
            OrderStatusLAbel.setVisibility(View.VISIBLE);
        }
        return footerView;
    }

    void startCheckoutProcess(ShoppingCartListResponse shoppingCartListResponse) {
        //start the checkoutActitiy
        CheckOutIntent checkOutIntent = new CheckOutIntent(getContext());
        checkOutIntent.putExtra(SHOPPING_CART,shoppingCartListResponse);
        startActivity(checkOutIntent);
    }

    private boolean initializeShoppingCartPage(final ShoppingCartListResponse shoppingCartListResponse){
        mContainerLayoutItems.setAdapter(new ShoppingCartRecyclerViewAdapter(shoppingCartListResponse.getShoppingCart().getCommerceItems(),createFooter(((LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.view_offer_code_card, null, false),shoppingCartListResponse),getActivity(),CartFragmentMessageHandler));

        if (sPreviousScrollPosition > 0){
            mContainerLayoutItems.scrollToPosition(sPreviousScrollPosition);
            sPreviousScrollPosition = 0;
        }

        if(null != shoppingCartListResponse.getShoppingCart()){
            ((TextView)(mTotalCheckOutContainer.findViewById(R.id.items_total_amt_txt))).setText(getActivity().getResources().getString(R.string.dollar_placeholder) + Float.toString(shoppingCartListResponse.getShoppingCart().getItemsTotal()));
            ((TextView)(mTotalCheckOutContainer.findViewById(R.id.subtotal_value_txt))).setText(getActivity().getResources().getString(R.string.dollar_placeholder) + Float.toString(shoppingCartListResponse.getShoppingCart().getSubTotal()));

            ((Button) mTotalCheckOutContainer.findViewById(R.id.button_checkout)).setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    if(null != shoppingCartListResponse.getShoppingCart()){
                        startCheckoutProcess(shoppingCartListResponse);
                    }
                }
            });

        } else {
            ((TextView)(mTotalCheckOutContainer.findViewById(R.id.items_total_amt_txt))).setText("-");
            ((TextView)(mTotalCheckOutContainer.findViewById(R.id.subtotal_value_txt))).setText("-");
        }

        if(null != shoppingCartListResponse.getShoppingCart()){
            ((TextView) mTotalCheckOutContainer.findViewById(R.id.offer_code_value_txt)).setText(getActivity().getResources().getString(R.string.dollar_placeholder) + Float.toString(shoppingCartListResponse.getShoppingCart().getDiscountAmount()));
        } else {
            ((TextView) mTotalCheckOutContainer.findViewById(R.id.offer_code_value_txt)).setText("-");
        }

        if (shoppingCartListResponse.getShippingMessageInfo().getIsFreeShipping()){
            mTotalCheckOutContainer.findViewById(R.id.shipping_description_layout).setVisibility(View.VISIBLE);
            ((TextView)(mTotalCheckOutContainer.findViewById(R.id.standard_shipping_txt))).setText(getActivity().getResources().getString(R.string.your_order_qualifies_for_free_standard_shipping));
        } else if (!shoppingCartListResponse.getShippingMessageInfo().getIsFreeShipping() && null != shoppingCartListResponse.getShippingMessageInfo().getMessage() && shoppingCartListResponse.getShippingMessageInfo().getMessage().length()>0){
            mTotalCheckOutContainer.findViewById(R.id.shipping_description_layout).setVisibility(View.VISIBLE);
            ((TextView)(mTotalCheckOutContainer.findViewById(R.id.standard_shipping_txt))).setText(shoppingCartListResponse.getShippingMessageInfo().getMessage().trim());
        } else {
            ((TextView)(mTotalCheckOutContainer.findViewById(R.id.standard_shipping_txt))).setText("-");
            mTotalCheckOutContainer.findViewById(R.id.shipping_description_layout).setVisibility(View.GONE);
        }
        return true;
    }

    private void toggleVisibilityShoppingList(boolean isEmpty){
        if (!isEmpty){
            mTotalCheckOutContainer.setVisibility(View.VISIBLE);
            mItemListtContainer.setVisibility(View.VISIBLE);
            mEmptyCheckoutContainer.setVisibility(View.GONE);
        }
        else if (isEmpty){
            mTotalCheckOutContainer.setVisibility(View.GONE);
            mItemListtContainer.setVisibility(View.GONE);
            mEmptyCheckoutContainer.setVisibility(View.VISIBLE);
        }
    }

    private void callmShoppingCartAPI(Object object){

        if (mPresenter == null){
            mPresenter = new ShoppingCartListPresenter(this);
        }
        if (object == null)
        {
            toggleProgressDialogVisibility(SHOW_PROGRESSBAR_OR_ANIMATION,mProgressBar);
            mPresenter.getGeneralPopulateShoppingCart();
        } else
        if (object instanceof AddItemRequestShoppingCart)
        {
            mPresenter.getAddItemShoppingCart((AddItemRequestShoppingCart)(object));
        } else
        if (object instanceof RemoveItemRequestShoppingCart)
        {
            toggleGIFAnimantionVisibility(SHOW_PROGRESSBAR_OR_ANIMATION,getActivity());
            mPresenter.getRemoveItemShoppingCart((RemoveItemRequestShoppingCart)(object));
        } else
        if (object instanceof ApplyCouponRequestShoppingCart)
        {
            toggleGIFAnimantionVisibility(SHOW_PROGRESSBAR_OR_ANIMATION,getActivity());
            mPresenter.getApplyCouponShoppingCart((ApplyCouponRequestShoppingCart)(object));
        } else
        if (object instanceof UpdateItemQuantityRequestShoppingCart)
        {
            toggleGIFAnimantionVisibility(SHOW_PROGRESSBAR_OR_ANIMATION,getActivity());
            mPresenter.getUpdateItemQuantityRequestShoppingCart((UpdateItemQuantityRequestShoppingCart)(object));
        }
    }

    public final Handler CartFragmentMessageHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            if (msg.what == Constants.DELETE_ITEM_REQUEST_SHOPPINGCART){
                callmShoppingCartAPI(new RemoveItemRequestShoppingCart(msg.getData().get(Constants.COMMERCE_ITEM_ID).toString(),null));
            } else if (msg.what == Constants.UPDATE_ITEM_QUANTITY_SHOPPINGCART){
                callmShoppingCartAPI(new UpdateItemQuantityRequestShoppingCart(((HashMap<String,String>)msg.getData().getSerializable(Constants.QUANTITY_MAP))));
            } else if (msg.what == Constants.CLICK_ITEM_UPDATE_SHOPPINGCART){
                CommonWebviewFragment commonWebviewFragment = new CommonWebviewFragment();
                commonWebviewFragment.setArguments(msg.getData());
                addStepRootChildFragment(commonWebviewFragment,R.id.cart_root_fragment_container);
            }
        }
    };

    public void UpdateValueThreadResponse(){
        try {
            ((HomeActivity)getActivity()).updateCartTabItemCount();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {

        PayPalCheckoutRequest payPalCheckoutRequest = new PayPalCheckoutRequest("cart", mPreferencesHelper.getSessionConfirmationResponse().getSessionConfirmationNumber());
        mPresenter.checkoutPayPal(payPalCheckoutRequest);


    }

}