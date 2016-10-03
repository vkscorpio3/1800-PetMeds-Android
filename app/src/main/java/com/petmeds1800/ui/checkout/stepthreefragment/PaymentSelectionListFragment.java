package com.petmeds1800.ui.checkout.stepthreefragment;

import com.petmeds1800.R;
import com.petmeds1800.model.Card;
import com.petmeds1800.ui.fragments.AbstractFragment;
import com.petmeds1800.ui.payment.AddEditCardFragment;
import com.petmeds1800.ui.payment.SavedCardsListContract;
import com.petmeds1800.ui.payment.SavedCardsListPresenter;
import com.petmeds1800.util.Utils;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;


/**
 * Created by Sdixit on 27-09-2016.
 */

public class PaymentSelectionListFragment extends AbstractFragment implements SavedCardsListContract.View {

    public static final int REQUEST_CODE = 5;

    @BindView(R.id.paymentList_recyclerView)
    RecyclerView mPaymentListRecyclerView;

    @BindView(R.id.progressbar)
    ProgressBar mProgressbar;

    @BindView(R.id.containerLayout)
    RelativeLayout mContainerLayout;

    private SavedCardsListContract.Presenter mPresenter;

    private Card mCard;

    private PaymentSelectionListAdapter mPaymentSelectionListAdapter;

    public static PaymentSelectionListFragment newInstance(int requestCode) {
        Bundle args = new Bundle();
        args.putInt(AddEditCardFragment.REQUEST_CODE, requestCode);
        PaymentSelectionListFragment fragment = new PaymentSelectionListFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPresenter = new SavedCardsListPresenter(this);

    }

    public void setCard(Card card) {
        if (getParentFragment() != null && getParentFragment() instanceof StepThreeRootFragment) {
            ((StepThreeRootFragment) getParentFragment()).setCard(card);
        }

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_payment_selction_list, container, false);
        ButterKnife.bind(this, view);
        mPaymentSelectionListAdapter = new PaymentSelectionListAdapter(this,
                getContext(), PaymentSelectionListFragment.REQUEST_CODE);
        setupCardsRecyclerView();
        return view;

    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    private void setupCardsRecyclerView() {
        mPaymentListRecyclerView.setAdapter(mPaymentSelectionListAdapter);
        mPaymentListRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mPaymentListRecyclerView.setNestedScrollingEnabled(false);
    }

    @Override
    public boolean isActive() {
        return isAdded();
    }

    @Override
    public void onResume() {
        super.onResume();
        mPresenter.start();
    }

    @Override
    public void showNoCardsView() {
        mProgressbar.setVisibility(View.GONE);
        mPaymentListRecyclerView.setVisibility(View.VISIBLE);
    }

    @Override
    public void showCardsListView(List<Card> cardsList) {
        mProgressbar.setVisibility(View.GONE);
        mPaymentListRecyclerView.setVisibility(View.VISIBLE);
        mPaymentSelectionListAdapter.setData(cardsList);
    }


    @Override
    public void showErrorMessage(String errorMessage) {
        mProgressbar.setVisibility(View.GONE);
        errorMessage = errorMessage.equals(Utils.TIME_OUT) ? getString(R.string.internet_not_available) : errorMessage;
        Utils.displayCrouton(getActivity(), errorMessage, mContainerLayout);
    }

    @Override
    public void startCardUpdate(Card card) {

    }

    @Override
    public void setPresenter(@NonNull SavedCardsListContract.Presenter presenter) {
        mPresenter = presenter;
    }


}
