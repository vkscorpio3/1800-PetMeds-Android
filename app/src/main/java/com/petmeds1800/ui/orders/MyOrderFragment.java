package com.petmeds1800.ui.orders;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import com.petmeds1800.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.petmeds1800.PetMedsApplication;
import com.petmeds1800.R;
import com.petmeds1800.dagger.component.DaggerOrderComponent;
import com.petmeds1800.dagger.module.OrderPresenterModule;
import com.petmeds1800.model.entities.OrderFilterList;
import com.petmeds1800.model.entities.OrderHistoryFilter;
import com.petmeds1800.model.entities.OrderList;
import com.petmeds1800.ui.AbstractActivity;
import com.petmeds1800.ui.HomeActivity;
import com.petmeds1800.ui.fragments.AbstractFragment;
import com.petmeds1800.ui.fragments.dialog.ItemSelectionDialogFragment;
import com.petmeds1800.ui.fragments.dialog.ItemSelectionDialogFragment.OnItemSelectedListener;
import com.petmeds1800.ui.orders.presenter.OrderListPresenter;
import com.petmeds1800.ui.orders.support.DividerItemDecoration;
import com.petmeds1800.ui.orders.support.MyOrderAdapter;
import com.petmeds1800.util.AnalyticsUtil;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by pooja on 8/3/2016.
 */
public class MyOrderFragment extends AbstractFragment
        implements View.OnClickListener, OnItemSelectedListener, OrderListContract.View {

    @BindView(R.id.order_list_view)
    RecyclerView mOrderRecyclerView;

    @BindView(R.id.filter_button)
    FloatingActionButton mFilterButton;

    private MyOrderAdapter mOrderListAdapter;

    @Inject
    OrderListPresenter mOrderPresenter;

    private List<OrderList> mOrderList;

    @BindView(R.id.progressbar)
    ProgressBar progressBar;

    @BindView(R.id.order_empty_view)
    LinearLayout mOrderEmptyLayout;

    @BindView(R.id.order_title_view)
    RelativeLayout mOrderTitleLayout;

    @BindView(R.id.order_count_label)
    TextView mOrderCountlabel;

    @BindView(R.id.filter_name_label)
    TextView mFilterTitleLabel;

    @BindView(R.id.shopNow_button)
    Button mShopNowButton;


    private OrderHistoryFilter mOrderHistoryFilter;
    private String filterApplied ;
    private int mSelectedFilterIndex=-1;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new AnalyticsUtil().trackScreen(getString(R.string.label_order_history));
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_orders, null);
        ButterKnife.bind(this, view);
        ((AbstractActivity) getActivity()).setToolBarTitle(getActivity().getString(R.string.title_my_orders));

        mOrderListAdapter = new MyOrderAdapter(false, getActivity(), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = mOrderRecyclerView.getChildAdapterPosition(v);
                OrderList orderDetail = mOrderList.get(position);
                Bundle bundle = new Bundle();
                bundle.putSerializable("orderlist", orderDetail);
                replaceAccountFragmentWithBundle(new OrderDetailFragment(), bundle);
            }
        });

        DaggerOrderComponent.builder()
                .appComponent(PetMedsApplication.getAppComponent())
                .orderPresenterModule(new OrderPresenterModule(this))
                .build().inject(this);
        setUpOrderList();
        mOrderPresenter.setOrderListData();
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mFilterButton.setOnClickListener(this);
        mShopNowButton.setOnClickListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        ((AbstractActivity) getActivity()).enableBackButton();
    }

    private void setUpOrderList() {
        mOrderRecyclerView.setAdapter(mOrderListAdapter);
        mOrderRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity()));
        mOrderRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mOrderRecyclerView.setHasFixedSize(true);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.filter_button:
                showFilterDialog();
                break;
            case R.id.shopNow_button:
                ((HomeActivity)getActivity()).getViewPager().setCurrentItem(0);
                break;
        }
    }


    @Override
    public void onItemSelected(ItemSelectionDialogFragment fragment, ItemSelectionDialogFragment.Item item, int index) {
        Log.d("item code",item.getCode());
        mSelectedFilterIndex=index;
        fragment.dismiss();
        progressBar.setVisibility(View.VISIBLE);
        filterApplied=item.getTitle();
        mOrderPresenter.getFilteredOrderList(item.getCode(),item.getTitle());
    }

    @Override
    public void setPresenter(OrderListContract.Presenter presenter) {

    }

    @Override
    public boolean isActive() {
        return isAdded();
    }


    @Override
    public void onError(String errorMessage) {
        progressBar.setVisibility(View.GONE);
        Snackbar.make(mOrderRecyclerView, errorMessage, Snackbar.LENGTH_LONG).show();

    }


    @Override
    public void updateOrderList(List<OrderList> orderList, String filterApplied) {
        progressBar.setVisibility(View.GONE);
        mOrderList = orderList;
        if (orderList.size() > 0) {
            mOrderRecyclerView.setVisibility(View.VISIBLE);
            mOrderEmptyLayout.setVisibility(View.GONE);
            mOrderTitleLayout.setVisibility(View.VISIBLE);
            mOrderCountlabel
                    .setText(String.valueOf(orderList.size()) + " " + getActivity().getString(R.string.title_orders));
            mFilterTitleLabel.setText(filterApplied);
            mOrderListAdapter.setData(orderList);
            mFilterButton.setVisibility(View.VISIBLE);
        } else {
            mOrderEmptyLayout.setVisibility(View.VISIBLE);
            mOrderTitleLayout.setVisibility(View.GONE);
            mOrderRecyclerView.setVisibility(View.GONE);
            mFilterButton.setVisibility(View.GONE);
        }

    }



    @Override
    public void updateFilterList(OrderHistoryFilter orderHistoryFilter) {
        this.mOrderHistoryFilter=orderHistoryFilter;
    }

    private void showFilterDialog(){
        ArrayList<ItemSelectionDialogFragment.Item> filterItems = new ArrayList<>();
        for(OrderFilterList orderFilterList :mOrderHistoryFilter.getOrderFilterList()){
            filterItems.add(new ItemSelectionDialogFragment.Item(orderFilterList.getName(), orderFilterList.isDefault(),orderFilterList.getCode()));

        }

        ItemSelectionDialogFragment dialog = ItemSelectionDialogFragment.newInstance(
                getActivity().getString(R.string.application_name),
                filterItems,
                mSelectedFilterIndex
        );
        dialog.setItemSelectionListener(this);
        dialog.show(getFragmentManager(), "ItemPicker");
    }

}
