package com.petmeds1800.ui;

import com.mtramin.rxfingerprint.RxFingerprint;
import com.petmeds1800.PetMedsApplication;
import com.petmeds1800.R;
import com.petmeds1800.api.PetMedsApiService;
import com.petmeds1800.intent.AddUpdateMedicationRemindersIntent;
import com.petmeds1800.model.Address;
import com.petmeds1800.model.entities.CommitOrderResponse;
import com.petmeds1800.model.entities.SecurityStatusResponse;
import com.petmeds1800.ui.fragments.AccountRootFragment;
import com.petmeds1800.ui.fragments.CartFragment;
import com.petmeds1800.ui.fragments.CartRootFragment;
import com.petmeds1800.ui.fragments.HomeRootFragment;
import com.petmeds1800.ui.fragments.LearnRootFragment;
import com.petmeds1800.ui.fragments.dialog.FingerprintAuthenticationDialog;
import com.petmeds1800.ui.fragments.dialog.ProgressDialog;
import com.petmeds1800.ui.medicationreminders.AddEditMedicationRemindersFragment;
import com.petmeds1800.ui.medicationreminders.MedicationReminderItemListContract;
import com.petmeds1800.ui.payment.AddACardContract;
import com.petmeds1800.ui.payment.AddEditCardFragment;
import com.petmeds1800.ui.support.TabPagerAdapter;
import com.petmeds1800.util.AnalyticsUtil;
import com.petmeds1800.util.AsyncUpdateShoppingCartIconCountThread;
import com.petmeds1800.util.Constants;
import com.petmeds1800.util.GeneralPreferencesHelper;
import com.petmeds1800.util.RetrofitErrorHandler;
import com.petmeds1800.util.Utils;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class HomeActivity extends AbstractActivity
        implements AddACardContract.AddressSelectionListener,
        MedicationReminderItemListContract.AddEditMedicationReminderListener, DialogInterface.OnClickListener {

    @BindView(R.id.tablayout)
    TabLayout mHomeTab;

    @BindView(R.id.viewpager_fragments)
    ViewPager mViewPager;

    @BindView(R.id.container_home)
    RelativeLayout mContainerLayout;

    @Inject
    PetMedsApiService mApiService;

    @Inject
    GeneralPreferencesHelper mPreferencesHelper;

    private ProgressDialog mProgressDialog;

    private FingerprintAuthenticationDialog mAuthenticationDialog;

    private static final String IS_FROM_HOME_ACTIVITY = "isFromHomeActivity";

    private int mTabIndex;

    private static final int[] TAB_ICON_UNSELECTED = {R.drawable.ic_menu_home, R.drawable.ic_menu_cart,
            R.drawable.ic_menu_learn, R.drawable.ic_menu_account};

    private static final int[] TAB_ICON_SELECTED = {R.drawable.ic_menu_home_pressed, R.drawable.ic_menu_cart_pressed,
            R.drawable.ic_menu_learn_pressed, R.drawable.ic_menu_account_pressed};

    TabPagerAdapter mAdapter;

    ArrayList<View> mTabLayoutArray = new ArrayList<>();

    private CommitOrderResponse commitOrderResponse;

    private CartRootFragment mCartRootFragment;

    @Override
    protected void onNewIntent(Intent intent) {
        // TODO Auto-generated method stub
        Log.v("on new intent fired", "on new intent fired");
        super.onNewIntent(intent);
        setIntent(intent);
        commitOrderResponse = (CommitOrderResponse) getIntent().getSerializableExtra(
                Constants.CONFIRMATION_ORDER_RESPONSE);
        mCartRootFragment = ((CartRootFragment) mAdapter.getItem(1));
        mCartRootFragment.replaceConfirmOrderFragment(commitOrderResponse);

    }

    //Call to replace receipt fragment on page listener
    private void replaceFragmentOnConfirmReceipt() {
        if (commitOrderResponse != null) {
            commitOrderResponse = null;
            mCartRootFragment.replaceConfirmOrderFragment(commitOrderResponse);

        }
    }
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        onCreate(savedInstanceState);
    }
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.bind(this);
        mAnalyticsUtil = new AnalyticsUtil();
        PetMedsApplication.getAppComponent().inject(this);

        if (getIntent().getBooleanExtra(IS_FROM_HOME_ACTIVITY, false) && mPreferencesHelper.getIsUserLoggedIn()) {
            showPushPermissionDailog();
            if (!RxFingerprint.isHardwareDetected(this)) {
                mPreferencesHelper.setIsFingerPrintEnabled(false);
            } else if (!RxFingerprint.hasEnrolledFingerprints(this)) {
                mPreferencesHelper.setIsFingerPrintEnabled(false);
            } else {
                mPreferencesHelper.setIsFingerPrintEnabled(true);
            }
            startService(new AddUpdateMedicationRemindersIntent(this, false));

        }


        //initialize fragment list
        List<Fragment> fragmentList = new ArrayList<>();
        HomeRootFragment mHomeRootFragment = new HomeRootFragment();
        mCartRootFragment = new CartRootFragment();
        fragmentList.add(mHomeRootFragment);
        fragmentList.add(mCartRootFragment);
        fragmentList.add(new LearnRootFragment());
        fragmentList.add(new AccountRootFragment());
        mAdapter = new TabPagerAdapter(getSupportFragmentManager(), fragmentList);
        mViewPager.setAdapter(mAdapter);
        mHomeTab.setupWithViewPager(mViewPager);

        for (int i = 0; i < mHomeTab.getTabCount(); i++) {
            View v = ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.custom_tab_image_overlay, null, false);
            mTabLayoutArray.add(i, v);
            mHomeTab.getTabAt(i).setCustomView(mTabLayoutArray.get(i));
        }

        mHomeTab.setOnTabSelectedListener(
                new TabLayout.ViewPagerOnTabSelectedListener(mViewPager) {
                    @Override
                    public void onTabSelected(TabLayout.Tab tab) {
                        super.onTabSelected(tab);
                        int numTab = tab.getPosition();
                        Log.d("ontabselected", numTab + ">>>>");
                    }

                    @Override
                    public void onTabUnselected(TabLayout.Tab tab) {
                        super.onTabUnselected(tab);
                        //TODO: could not understand the purpose of this code so removed. If still needed please justify
                        removeAllFragment();
                    }
                });

        ViewPager.OnPageChangeListener pageChangeListener = new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

                replaceFragmentOnConfirmReceipt();
                Log.d("onPageSelected", ">>>>>>");
                mTabIndex = position;
                for (int i = 0; i < mHomeTab.getTabCount(); ++i) {
                    ((ImageView) (mHomeTab.getTabAt(i).getCustomView()).findViewById(R.id.tab_default_image))
                            .setImageResource(i != position ? TAB_ICON_UNSELECTED[i] : TAB_ICON_SELECTED[i]);

                }

                if (position == 3 && mPreferencesHelper.getIsUserLoggedIn()) {
                    //TODO: code improvement, We can create constants for the pages
                    Log.v("test", "from scroll tab>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
                    checkLoginStatus();
                }

                if (position == 0) {
                    getToolbar().setLogo(R.drawable.ic_logo_petmeds_toolbar);

                } else {
                    getToolbar().setLogo(null);
                }

                invalidateOptionsMenu();
                setToolBarTitle((getResources().getStringArray(R.array.tab_title)[position]));
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }

        };

        //code to set default first tab selected
        mViewPager.addOnPageChangeListener(pageChangeListener);

      /*  if (commitOrderResponse != null) {
            mViewPager.setCurrentItem(1);
        } else {
            mViewPager.setCurrentItem(0);
        }*/
        pageChangeListener.onPageSelected(0);
        // Instead of initializing it in onResume for many times we initialize it in onCreate
        mProgressDialog = new ProgressDialog();
        mAuthenticationDialog = new FingerprintAuthenticationDialog();
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_home;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mTabIndex == 3 && mPreferencesHelper.getIsUserLoggedIn()) {
            checkLoginStatus();
        }
        invalidateOptionsMenu();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mProgressDialog != null && mProgressDialog.isVisible()) {
            mProgressDialog.dismiss();
        }
        if (mAuthenticationDialog != null && mAuthenticationDialog.isVisible()) {
            mAuthenticationDialog.dismiss();
        }
    }

    @Override
    public void setAddress(Address address, int requestCode) {
        AddEditCardFragment addCardFragment = (AddEditCardFragment) getSupportFragmentManager()
                .findFragmentByTag(AddEditCardFragment.class.getName());
        if (addCardFragment != null) {
            addCardFragment.displayAddress(address);
        } else {
            AddEditCardFragment newFragment = AddEditCardFragment.newInstance(address, requestCode);
            replaceAccountAndAddToBackStack(newFragment, AddEditCardFragment.class.getName());
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home) {
            super.onBackPressed();
        }
        return super.onOptionsItemSelected(menuItem);
    }

    private void showFingerprintDialog() {
        mAuthenticationDialog.setCancelable(false);
        mAuthenticationDialog.show(getSupportFragmentManager(), "FingerprintAuthenticationDialog");
    }

    public void removeAllFragment() {
        Log.d("remove all frgamnet", ">>>>");
        while (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStackImmediate();
        }
    }

    public ViewPager getViewPager() {
        return mViewPager;
    }

    public void showPushPermissionDailog() {
        AlertDialog alertDialog = Utils.showAlertDailog(this,
                String.format(getString(R.string.notification_title), getString(R.string.application_name)),
                getString(R.string.notification_message), R.style.StyleForNotification)
                .setCancelable(false)
                .setPositiveButton(getString(R.string.dialog_allow_button).toUpperCase(), this)
                .setNegativeButton(getString(R.string.dialog_deny_button).toUpperCase(), this)
                .create();
        alertDialog.show();
    }

    public AnalyticsUtil getAnalyticsRef() {
        return mAnalyticsUtil;
    }

    private void checkLoginStatus() {
        showProgress();
        mApiService.getSecurityStatus()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<SecurityStatusResponse>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        hideProgress();
                        int errorId = RetrofitErrorHandler.getErrorMessage(e);
                        if (errorId == R.string.noInternetConnection) {
                            Toast.makeText(HomeActivity.this, getString(R.string.noInternetConnection),
                                    Toast.LENGTH_SHORT).show();
                        }
                        getViewPager().setCurrentItem(0);
                        Log.v("onError", e.getMessage());
                    }

                    @Override
                    public void onNext(SecurityStatusResponse securityStatusResponse) {
                        hideProgress();
                        int securityStatus = securityStatusResponse.getSecurityStatus();
                        Log.i("security status:", securityStatus + "");
                        //TODO: improvement
                        if (securityStatus == 0 || securityStatus
                                == 2) {  // We need to treat security status 2 same as 0 as all the API on AccountSection stopped working if we would treat 2 same as 4
                            showFingerprintDialog();
                        } else if (securityStatus == 4) {
                            //TODO: research more into silent sign in logic
                        }
                    }
                });
    }

    public void showProgress() {
        mProgressDialog.setCancelable(false);
        if (!mProgressDialog.isAdded()) {
            mProgressDialog.show(getSupportFragmentManager(), "ProgressDialog");
        }
    }

    public void hideProgress() {
        mProgressDialog.dismiss();
    }

    public ViewGroup getContainerView() {
        return mContainerLayout;
    }

    @Override
    protected void onApplyThemeResource(Resources.Theme theme, int resid, boolean first) {
        super.onApplyThemeResource(theme, resid, first);
    }


    @Override
    public void onClick(DialogInterface dialog, int which) {
        switch (which) {
            case DialogInterface.BUTTON_POSITIVE:
                mPreferencesHelper.setIsPushNotificationEnableFlag(true);
                mAnalyticsUtil.trackEvent(getString(R.string.push_notifications_category),
                        getString(R.string.push_notifications_enability),
                        getString(R.string.push_notifications_enable_label));
                break;
            case DialogInterface.BUTTON_NEGATIVE:
                mPreferencesHelper.setIsPushNotificationEnableFlag(false);
                mAnalyticsUtil.trackEvent(getString(R.string.push_notifications_category),
                        getString(R.string.push_notifications_disability),
                        getString(R.string.push_notification_disability));
                break;
        }
    }



    @Override
    public void setItemDescription(String productName, String description) {
        AddEditMedicationRemindersFragment addEditMedicationRemindersFragment
                = (AddEditMedicationRemindersFragment) getSupportFragmentManager()
                .findFragmentByTag(AddEditMedicationRemindersFragment.class.getName());
        if (addEditMedicationRemindersFragment != null) {
            addEditMedicationRemindersFragment.displayItemText(productName, description);
        } else {
            replaceAccountAndAddToBackStack(AddEditMedicationRemindersFragment.newInstance(false, null),
                    AddEditMedicationRemindersFragment.class.getName());
        }
    }

    /**
     * For updating BOTH contents of Shopping Cart and Tab Layout Quantity.
     * Internally calls updateCartMenuItemCount
     * called when contents are altered in Shopping Cart from Outside {@link CartFragment}
     */
    public void updateCartTabItemCount(){
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                new AsyncUpdateShoppingCartIconCountThread(mApiService,mPreferencesHelper,HomeActivityMessageHandler).startAsyncUpdateShoppingCartIconCountThread();
            }
        };
        new Handler().postDelayed(runnable,1000);
    }

    /**
     * For updating the TAB LAyout count for Shopping Cart ONLY. Does not call other APIs internally
     * Only TAB layout count changes. NOT content of ShoppingCart iteslf
     */
    public void updateCartMenuItemCount(){
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(new Intent(Constants.KEY_CART_FRAGMENT_INTENT_FILTER));
            }
        };
        new Handler().postDelayed(runnable,1000);
    }

    public final Handler HomeActivityMessageHandler = new Handler(){

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            if (msg.what == Constants.KEY_COMPLETED_ASYN_COUNT_FETCH) {
                if (msg.getData().getBoolean(Constants.KEY_SHOPPING_CART_ASYNC_SUCCESS)){
                    mTabLayoutArray.get(1).findViewById(R.id.tab_text_over_image_container).setVisibility(View.VISIBLE);
                    ((TextView)mTabLayoutArray.get(1).findViewById(R.id.tab_text_over_image)).setText(msg.getData().getString(Constants.KEY_SHOPPING_CART_ICON_VALUE));
                } else {
                    Snackbar.make(mContainerLayout,msg.getData().getString(Constants.KEY_SHOPPING_CART_ICON_VALUE), Snackbar.LENGTH_LONG).show();
                }
            }
        }
    };

    /**
     * Move View Pager Externaly via programtically assigning selection number
     * @param pageNo
     */
    public void scrollViewPager(int pageNo){
        mViewPager.setCurrentItem(pageNo);
    }
}