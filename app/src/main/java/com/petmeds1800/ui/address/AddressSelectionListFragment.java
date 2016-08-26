package com.petmeds1800.ui.address;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.petmeds1800.R;
import com.petmeds1800.model.Address;
import com.petmeds1800.ui.HomeActivity;
import com.petmeds1800.ui.fragments.AbstractFragment;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Abhinav on 11/8/16.
 */
public class AddressSelectionListFragment extends AbstractFragment implements SavedAddressListContract.View, View.OnClickListener {

    @BindView(R.id.noSavedAddress_layout)
    LinearLayout mNoSavedAddressLinearLayout;

    @BindView(R.id.savedAddress_recyclerView)
    RecyclerView mSavedAddressRecyclerView;

    private SavedAddressListContract.Presenter mPresenter;
    private AddressSelectionAdapter mSavedAddressAdapter;
    private MenuItem mAddMenuItem;
    private HomeActivity mCallback;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPresenter = new SavedAddressListPresenter(this);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_saved_address_list, container, false);
        ButterKnife.bind(this, view);

        mSavedAddressAdapter = new AddressSelectionAdapter(false,this,getContext());
        setupCardsRecyclerView();
        return view;

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mCallback = (HomeActivity) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement AddACardContract.AddressSelectionListener");
        }

    }

    private void setupCardsRecyclerView() {
        mSavedAddressRecyclerView.setAdapter(mSavedAddressAdapter);
        mSavedAddressRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
    }

    @Override
    public void onResume() {
        super.onResume();
        mPresenter.start();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_add_a_card, menu);
        mAddMenuItem = menu.findItem(R.id.action_add);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.action_add){
            replaceAndAddToBackStack(new AddEditAddressFragment() , AddEditAddressFragment.class.getName());
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean isActive() {
        return isAdded();
    }

    @Override
    public void showNoAddressView() {
        mNoSavedAddressLinearLayout.setVisibility(View.VISIBLE);
        mSavedAddressRecyclerView.setVisibility(View.VISIBLE);

    }

    @Override
    public void showAddressListView(List<Address> addressList) {
        mNoSavedAddressLinearLayout.setVisibility(View.GONE);
        mSavedAddressRecyclerView.setVisibility(View.VISIBLE);
        mSavedAddressAdapter.setData(addressList);
    }

    @Override
    public void startAddressUpdate(Address address) {
     // no implementation is required here. But implementation is required in SavedAddressListFragment
    }

    @Override
    public void setPresenter(@NonNull SavedAddressListContract.Presenter presenter) {
        mPresenter = presenter;
    }

    void forwardAddressToActivity(Address address) {
        if(mCallback != null) {
            mCallback.setAddress(address);
        }
    }

    @Override
    public void onClick(View v) {

    }
}
