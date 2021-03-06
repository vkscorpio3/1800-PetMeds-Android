package com.petmeds1800.ui.pets;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.github.jjobes.slidedatetimepicker.SlideDateTimeListener;
import com.github.jjobes.slidedatetimepicker.SlideDateTimePicker;
import com.petmeds1800.PetMedsApplication;
import com.petmeds1800.R;
import com.petmeds1800.model.entities.AddPetRequest;
import com.petmeds1800.model.entities.AgeListResponse;
import com.petmeds1800.model.entities.AlertDailogMultipleChoice;
import com.petmeds1800.model.entities.BreedItem;
import com.petmeds1800.model.entities.MedAllergy;
import com.petmeds1800.model.entities.NameValueData;
import com.petmeds1800.model.entities.PetBreedTypeListResponse;
import com.petmeds1800.model.entities.PetMedicalConditionResponse;
import com.petmeds1800.model.entities.PetMedicationResponse;
import com.petmeds1800.model.entities.PetTypesListResponse;
import com.petmeds1800.model.entities.Pets;
import com.petmeds1800.model.entities.RemovePetRequest;
import com.petmeds1800.ui.AbstractActivity;
import com.petmeds1800.ui.HomeActivity;
import com.petmeds1800.ui.checkout.AddNewEntityActivity;
import com.petmeds1800.ui.fragments.AbstractFragment;
import com.petmeds1800.ui.fragments.dialog.CommonDialogFragment;
import com.petmeds1800.ui.fragments.dialog.GenderDialogFragment;
import com.petmeds1800.ui.medicationreminders.AddPetNameListener;
import com.petmeds1800.ui.pets.presenter.AddPetPresenter;
import com.petmeds1800.ui.pets.support.AddPetContract;
import com.petmeds1800.ui.pets.support.DialogOptionEnum;
import com.petmeds1800.ui.pets.support.UpdateImageUtil;
import com.petmeds1800.util.AlertRecyclerView;
import com.petmeds1800.util.AlertRecyclerViewAdapter;
import com.petmeds1800.util.AnalyticsUtil;
import com.petmeds1800.util.GeneralPreferencesHelper;
import com.petmeds1800.util.Log;
import com.petmeds1800.util.Utils;
import com.soundcloud.android.crop.Crop;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

import static com.petmeds1800.R.id.fromGallery;

/**
 * Created by pooja on 8/23/2016.
 */
public class AddPetFragment extends AbstractFragment
        implements View.OnClickListener, GenderDialogFragment.GenderSetListener,
        CommonDialogFragment.ValueSelectedListener, AddPetContract.View, DialogInterface.OnClickListener {

    private static final int AGE_REQUEST = 1;

    private static final int BREED_REQUEST = 2;

    private static final int TYPE_REQUEST = 3;

    public static final String MULTIPART_FORM_DATA = "multipart/form-data";

    @BindView(R.id.pet_gender_edit)
    EditText mPetGenderText;

    @BindView(R.id.pet_birthday_edit)
    EditText mPetBirthdayText;

    @BindView(R.id.pet_age_edit)
    EditText mPetAgeText;

    @BindView(R.id.pet_picture_edit)
    EditText mPetPictureText;

    UpdateImageUtil updateImageUtil;

    @BindView(R.id.pet_picture_image)
    ImageView mPetImage;

    @BindView(R.id.pet_name_edit)
    EditText mPetNameText;

    @BindView(R.id.owner_name_edit)
    EditText mOwnerNameText;

    @BindView(R.id.pet_type_edit)
    EditText mPetTypeText;

    @BindView(R.id.breed_type_edit)
    EditText mBreedTypeText;

    @BindView(R.id.pet_weight_edit)
    EditText mPetWeight;

    @BindView(R.id.medication_allery_title)
    TextView mMedicationAlleryTitle;

    @BindView(R.id.add_edit_medication_allergies)
    TextView mAddEditMedicationAllergies;

    @BindView(R.id.medication_allergies_details)
    TextView mMedicationAllergiesDetails;

    @BindView(R.id.medical_condition_title)
    TextView mMedicalConditionTitle;

    @BindView(R.id.add_edit_medication_conditions)
    TextView mAddEditMedicationConditions;

    @BindView(R.id.medication_conditions_details)
    TextView mMedicationConditionsDetails;

    @BindView(R.id.petContainerLayout)
    RelativeLayout mContainerLayout;

    @BindView(R.id.current_medications_title)
    TextView mCurrentMedicationsTitle;

    @BindView(R.id.add_edit_current_medications)
    TextView mAddEditCurrentMedications;

    @BindView(R.id.current_medications_details)
    TextView mCurrentMedicationsDetails;


    private ArrayList<Integer> medConditionIds;

    @Inject
    GeneralPreferencesHelper mPreferencesHelper;

    @BindView(R.id.petNameInputLayout)
    TextInputLayout mPetNameInputLayout;

    @BindView(R.id.ownerNameInputLayout)
    TextInputLayout mOwnerNameInputLayout;

    @BindView(R.id.petTypeInputLayout)
    TextInputLayout mPetTypeInputLayout;

    @BindView(R.id.breedInputLayout)
    TextInputLayout mBreedInputLayout;

    @BindView(R.id.genderInputLayout)
    TextInputLayout mGenderInputLayout;

    @BindView(R.id.ageInputLayout)
    TextInputLayout mAgeInputLayout;

    @BindView(R.id.weightInputLayout)
    TextInputLayout mWeightInputLayout;

    @BindView(R.id.birthdayInputLayout)
    TextInputLayout mBirthdayInputLayout;

    private AddPetContract.Presenter mPresenter;

    long birthdayInMillis;

    @BindView(R.id.progressbar)
    ProgressBar progressBar;

    View dialoglayout;

    AlertDialog alertDailogForPicture;

    private boolean isEditable;

    @BindView(R.id.pet_image_layout)
    RelativeLayout editPetImageView;

    @BindView(R.id.add_pet_image_layout)
    RelativeLayout addPetImageView;

    private MenuItem mEditMenuItem;

    private MenuItem mDoneMenuItem;

    private Pets mPet;

    @BindView(R.id.remove_pet_button)
    Button removePetButton;

    @BindView(R.id.edit_pet_image)
    ImageView mEditPetImage;

    public static final int GENDER_REQUEST_CODE = 1;

    public static final int REMOVE_PET_REQUEST_CODE = 2;


    private AlertRecyclerViewAdapter mAlertRecyclerViewAdapter;

    private ArrayList<AlertDailogMultipleChoice> petMedicationAlleryList;

    private ArrayList<AlertDailogMultipleChoice> petMedicationConditionList;

    private ArrayList<AlertDailogMultipleChoice> petCurrentMedicationList;

    private ArrayList<NameValueData> petAgeData;

    private AlertRecyclerView recyclerView;

    private ArrayList<NameValueData> nameValueDetailsForMedicalConditons;

    private ArrayList<NameValueData> nameValueDetailsForMedicalallergy;

    private ArrayList<NameValueData> nameValueDetailsForCurrentMedications;

    public static int MEDICATION_ALLERGY_1 = 0;

    public static int MEDICATION_ALLERGY_2 = 1;

    public static int MEDICATION_ALLERGY_3 = 2;

    public static int MEDICATION_ALLERGY_4 = 3;

    public static final String NEXT_LINE = "\n";

    public static final int IS_MEDICATIONS_ALLERGY_DAILOG = 5;

    public static final int IS_MEDICATIONS_CONDITIONS_DAILOG = 6;

    public static final int IS_CURRENT_MEDICATIONS_DAILOG = 7;

    private int fromWhichDailog = 0;

    private String age = "";

    private AddNewEntityActivity mCallback;

    private AddPetNameListener mAddPetNameListener;

    private String mSelectedPetType;

    private Uri finalUri;

    private Pets mPets;

    private Uri mCropImageUri;

    @BindView(R.id.container_layout)
    LinearLayout mViewContainer;

    public void setAddPetNameListener(AddPetNameListener addPetNameListener) {
        mAddPetNameListener = addPetNameListener;
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_pet, container, false);
        ButterKnife.bind(this, view);
        isEditable = getArguments().getBoolean("isEditable");
        mPet = (Pets) getArguments().getSerializable("pet");

        if (isEditable) {
            if (mPet != null) {
                ((AbstractActivity) getActivity()).setToolBarTitle(mPet.getPetName());
            }
            new AnalyticsUtil().trackScreen(getString(R.string.label_edit_pet));
            editPetImageView.setVisibility(View.VISIBLE);
            addPetImageView.setVisibility(View.GONE);
            removePetButton.setVisibility(View.VISIBLE);
            setPetData(mPet);
            enableViews(false);

        } else {
            ((AbstractActivity) getActivity()).setToolBarTitle(getActivity().getString(R.string.title_add_pet));
            new AnalyticsUtil().trackScreen(getString(R.string.label_add_pet));
            addPetImageView.setVisibility(View.VISIBLE);
            editPetImageView.setVisibility(View.GONE);
            removePetButton.setVisibility(View.GONE);
            enableViews(true);

        }

        return view;
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mPresenter = new AddPetPresenter(this, getActivity());
        setHasOptionsMenu(true);
        PetMedsApplication.getAppComponent().inject(this);
        mPetGenderText.setOnClickListener(this);
        mPetBirthdayText.setOnClickListener(this);
        mPetAgeText.setOnClickListener(this);
        mPetPictureText.setOnClickListener(this);
        removePetButton.setOnClickListener(this);
        mEditPetImage.setOnClickListener(this);
        mBreedTypeText.setOnClickListener(this);
        mPetTypeText.setOnClickListener(this);
        mAddEditMedicationAllergies.setOnClickListener(this);
        mAddEditMedicationConditions.setOnClickListener(this);
        mAddEditCurrentMedications.setOnClickListener(this);
        updateImageUtil = UpdateImageUtil.getInstance(this);


    }

    public void openDailog(String data[], int code, String title) {
        //((HomeActivity) getActivity()).hideProgress();
        FragmentManager fragManager = getFragmentManager();
        CommonDialogFragment commonDialogFragment = CommonDialogFragment
                .newInstance(data,
                        title, code);
        commonDialogFragment.setValueSetListener(this);
        commonDialogFragment.show(fragManager);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.pet_gender_edit:
                FragmentManager fm = getFragmentManager();
                GenderDialogFragment dialogFragment = new GenderDialogFragment();
                Bundle bundle = new Bundle();
                bundle.putStringArray("options", getResources().getStringArray(R.array.gender_array));
                bundle.putString("title", getString(R.string.choose_gender_label));
                bundle.putString("message", "");
                bundle.putString("ok", getString(R.string.dialog_ok_button));
                bundle.putString("cancel", getString(R.string.dialog_cancel_button));
                dialogFragment.setTargetFragment(this, GENDER_REQUEST_CODE);
                dialogFragment.setGenderSetListener(this);
                dialogFragment.setArguments(bundle);
                dialogFragment.show(fm);

                break;
            case R.id.pet_birthday_edit:
                new SlideDateTimePicker.Builder(getActivity().getSupportFragmentManager())
                        .setListener(listener).setMaxDate(Calendar.getInstance().getTime())
                        .setInitialDate(new Date())
                        .setIndicatorColor(getActivity().getResources().getColor(R.color.pattern_blue))
                        .build()
                        .show();
                break;
            case R.id.pet_age_edit:
                mPresenter.populatePetAgeList();
                break;
            case R.id.pet_picture_edit:
                // showImageOptions();
                if (CropImage.isExplicitCameraPermissionRequired(getActivity())) {
                    requestPermissions(new String[]{Manifest.permission.CAMERA},
                            CropImage.CAMERA_CAPTURE_PERMISSIONS_REQUEST_CODE);
                } else {
                    CropImage.startPickImageActivity(getActivity());
                }
                break;
            case R.id.takePhoto:
                updateImageUtil.updateProfilePic(UpdateImageUtil.CAMERA_CAPTURE_IMAGE_REQUEST_CODE);
                alertDailogForPicture.dismiss();
                break;
            case fromGallery:
                updateImageUtil.updateProfilePic(UpdateImageUtil.GALLERY_CAPTURE_IMAGE_REQUEST_CODE);
             //   alertDailogForPicture.dismiss();
                break;
            case R.id.remove_pet_button:
                FragmentManager fragmentManager = getFragmentManager();
                Bundle removePetBundle = new Bundle();
                removePetBundle.putStringArray("options", getResources().getStringArray(R.array.remove_pet_option));
                removePetBundle.putString("title", getString(R.string.remove_pet_title));
                removePetBundle.putString("message", getString(R.string.remove_pet_message));
                removePetBundle.putString("ok", getString(R.string.label_fingerprint_continue));
                removePetBundle.putString("cancel", getString(R.string.cancelTextOnDialog));
                GenderDialogFragment removePetDialog = new GenderDialogFragment();
                removePetDialog.setTargetFragment(this, REMOVE_PET_REQUEST_CODE);
                removePetDialog.setGenderSetListener(this);
                removePetDialog.setArguments(removePetBundle);
                removePetDialog.show(fragmentManager);
                break;
            case R.id.edit_pet_image:
                // showImageOptions();
                if (CropImage.isExplicitCameraPermissionRequired(getActivity())) {
                    requestPermissions(new String[]{Manifest.permission.CAMERA},
                            CropImage.CAMERA_CAPTURE_PERMISSIONS_REQUEST_CODE);
                } else {
                    CropImage.startPickImageActivity(getActivity());
                }
                break;
            case R.id.add_edit_medication_allergies:
                // ((HomeActivity) getActivity()).showProgress();
                fromWhichDailog = IS_MEDICATIONS_ALLERGY_DAILOG;
                if (petMedicationAlleryList != null && petMedicationAlleryList.size() > 0) {
                    showPetMedicalData(getString(R.string.title_for_pet_allergies), petMedicationAlleryList);
                } else {
                    mPresenter.populatePetMedicationsList();
                }
                break;
            case R.id.add_edit_medication_conditions:
                //   ((HomeActivity) getActivity()).showProgress();
                fromWhichDailog = IS_MEDICATIONS_CONDITIONS_DAILOG;
                if (petMedicationConditionList != null && petMedicationConditionList.size() > 0) {
                    showPetMedicalData(getString(R.string.title_for_pet_conditions), petMedicationConditionList);
                } else {
                    mPresenter.populatePetMedicalconditionsList();
                }
                break;
            case R.id.add_edit_current_medications:
                fromWhichDailog = IS_CURRENT_MEDICATIONS_DAILOG;
                if (petCurrentMedicationList != null && petCurrentMedicationList.size() > 0) {
                    showPetMedicalData(getString(R.string.current_medication_title), petCurrentMedicationList);
                } else {
                    mPresenter.populatePetMedicationsList();
                }

                break;
            case R.id.pet_type_edit:
                //  ((HomeActivity) getActivity()).showProgress();
                mPresenter.populatePetTypeList();
                break;
            case R.id.breed_type_edit:
                //  ((HomeActivity) getActivity()).showProgress();
                mSelectedPetType = mPetTypeText.getText().toString();
                mPresenter.pouplatePetBreedTypeList();
                break;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        ((AbstractActivity) getActivity()).enableBackButton();
    }

    @Override
    public void onGenderSet(GenderDialogFragment fragment, String gender, int key) {
        if (fragment.getTargetRequestCode() == GENDER_REQUEST_CODE) {
            mPetGenderText.setText(gender);
        } else if (fragment.getTargetRequestCode() == REMOVE_PET_REQUEST_CODE) {
            RemovePetRequest request = new RemovePetRequest(mPet.getPetId(), String.valueOf(key),
                    mPreferencesHelper.getSessionConfirmationResponse().getSessionConfirmationNumber());
            mPresenter.removePet(request , key);

        }
    }

    private SlideDateTimeListener listener = new SlideDateTimeListener() {

        @Override
        public void onDateTimeSet(Date date) {
            birthdayInMillis = date.getTime();
            mPetBirthdayText.setText(Utils.changeDateFormat(date.getTime(), "MM/dd/yyyy"));
            Log.d("pet birthday", date.getTime()+">>>");
        }

        // Optional cancel listener
        @Override
        public void onDateTimeCancel() {

        }
    };

    @Override
    public void onValueSelected(String value, int requestCode) {

        switch (requestCode) {
            case AGE_REQUEST:
                mPetAgeText.setText(value);
                break;
            case BREED_REQUEST:
                mBreedTypeText.setText(value);
                break;
            case TYPE_REQUEST:
                mPetTypeText.setText(value);
                break;
        }

    }

    private void showImageOptions() {
        dialoglayout = LayoutInflater.from(getActivity()).inflate(
                R.layout.dialog_for_picture, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.selectPetTitle));
        builder.setView(dialoglayout);
        alertDailogForPicture = builder.create();
        alertDailogForPicture.show();
        TextView takePhoto = (TextView) dialoglayout.findViewById(R.id.takePhoto);
        TextView fromGallery = (TextView) dialoglayout.findViewById(R.id.fromGallery);
        takePhoto.setTextColor(getActivity().getResources().getColor(R.color.petmeds_blue));
        fromGallery.setTextColor(getActivity().getResources().getColor(R.color.petmeds_blue));
        takePhoto.setOnClickListener(this);
        fromGallery.setOnClickListener(this);
    }


    @Override
    @SuppressLint("NewApi")
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.PICK_IMAGE_CHOOSER_REQUEST_CODE && resultCode == AppCompatActivity.RESULT_OK) {
            Uri imageUri = CropImage.getPickImageResultUri(getActivity(), data);

            // For API >= 23 we need to check specifically that we have permissions to read external storage,
            // but we don't know if we need to for the URI so the simplest is to try open the stream and see if we get error.
            boolean requirePermissions = false;
            if (CropImage.isReadExternalStoragePermissionsRequired(getActivity(), imageUri)) {

                // request permissions and handle the result in onRequestPermissionsResult()
                requirePermissions = true;
                mCropImageUri = imageUri;
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        CropImage.PICK_IMAGE_PERMISSIONS_REQUEST_CODE);
            } else {
                CropImage.activity(imageUri).setCropShape(CropImageView.CropShape.OVAL)
                        .start(getContext(), this);
                // mCropImageView.setImageUriAsync(imageUri);

            }

        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == AppCompatActivity.RESULT_OK) {
                finalUri = result.getUri();
                Log.d("imageuri", finalUri.toString() + isEditable);
                if (isEditable) {
                    Glide.with(this).load(finalUri.toString()).asBitmap().diskCacheStrategy(DiskCacheStrategy.NONE)
                            .skipMemoryCache(true).centerCrop().into(new BitmapImageViewTarget(mEditPetImage) {
                        @Override
                        protected void setResource(Bitmap resource) {
                            RoundedBitmapDrawable circularBitmapDrawable = RoundedBitmapDrawableFactory
                                    .create(getActivity().getResources(), resource);
                            circularBitmapDrawable.setCircular(true);
                            mEditPetImage.setImageDrawable(circularBitmapDrawable);
                        }
                    });
                } else {
                    Glide.with(this).load(finalUri.toString()).asBitmap().diskCacheStrategy(DiskCacheStrategy.NONE)
                            .skipMemoryCache(true).centerCrop().into(new BitmapImageViewTarget(mPetImage) {
                        @Override
                        protected void setResource(Bitmap resource) {
                            RoundedBitmapDrawable circularBitmapDrawable = RoundedBitmapDrawableFactory
                                    .create(getActivity().getResources(), resource);
                            circularBitmapDrawable.setCircular(true);
                            mPetImage.setImageDrawable(circularBitmapDrawable);
                        }
                    });
                }

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    /* public void onActivityResult(int requestCode, int resultCode, Intent data) {
           super.onActivityResult(requestCode, resultCode, data);
           File mUri;
           if (resultCode == Activity.RESULT_OK) {
               switch (requestCode) {
                   case UpdateImageUtil.CAMERA_CAPTURE_IMAGE_REQUEST_CODE:
                       updateImageUtil.onActivityResult(requestCode, resultCode, data);
                       break;
                   case UpdateImageUtil.GALLERY_CAPTURE_IMAGE_REQUEST_CODE:
                       updateImageUtil.onActivityResult(requestCode, resultCode, data);
                       break;
                   case Crop.REQUEST_CROP:

                       handleCrop(resultCode, data);

                       break;
                   default:
                       break;
               }
           } else if (resultCode == Activity.RESULT_CANCELED) {
           }
       }*/
//TODO Remove code after QA approval of new cropping library
    protected void handleCrop(int resultCode, Intent result) {
        if (resultCode == Activity.RESULT_OK) {
            finalUri = Crop.getOutput(result);
            if (finalUri != null) {
                if (!isEditable) {
                    Glide.with(this).load(finalUri.toString()).asBitmap().diskCacheStrategy(DiskCacheStrategy.NONE)
                            .skipMemoryCache(true).centerCrop().into(new BitmapImageViewTarget(mPetImage) {
                        @Override
                        protected void setResource(Bitmap resource) {
                            RoundedBitmapDrawable circularBitmapDrawable =
                                    RoundedBitmapDrawableFactory.create(getActivity().getResources(), resource);
                            circularBitmapDrawable.setCircular(true);
                            mPetImage.setImageDrawable(circularBitmapDrawable);
                        }
                    });
                } else {
                    Glide.with(this).load(finalUri.toString()).asBitmap().diskCacheStrategy(DiskCacheStrategy.NONE)
                            .skipMemoryCache(true).centerCrop().into(new BitmapImageViewTarget(mEditPetImage) {
                        @Override
                        protected void setResource(Bitmap resource) {
                            RoundedBitmapDrawable circularBitmapDrawable =
                                    RoundedBitmapDrawableFactory.create(getActivity().getResources(), resource);
                            circularBitmapDrawable.setCircular(true);
                            mEditPetImage.setImageDrawable(circularBitmapDrawable);
                        }
                    });
                }
            }

        } else if (resultCode == Crop.RESULT_ERROR) {
            Toast.makeText(getContext(), Crop.getError(result).getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_account_settings, menu);
        mEditMenuItem = menu.findItem(R.id.action_edit);
        mDoneMenuItem = menu.findItem(R.id.action_done);
        if (isEditable) {
            enableEditAction();
        } else {
            enableDoneAction();
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        String medicationAllergyArray[] = new String[]{"", "", "", ""};
        String currentMedicationArray[] = new String[]{"", "", "", ""};
        if (id == R.id.action_edit) {
            enableDoneAction();
            enableViews(true);
            return super.onOptionsItemSelected(item);
        }
        if (id == R.id.action_done) {

            boolean isValidPetName;
            boolean isValidOwnerName;
            boolean isValidPetType;
            boolean isValidBreedType;
            boolean isValidGender;
            boolean isValidWeight;
            boolean isValidAge;
            isValidPetName = checkAndShowError(mPetNameText, mPetNameInputLayout, R.string.error_petname);
            isValidOwnerName = checkAndShowError(mOwnerNameText, mOwnerNameInputLayout, R.string.error_petowner);
            isValidPetType = checkAndShowError(mPetTypeText, mPetTypeInputLayout, R.string.error_pettype);
            isValidBreedType = checkAndShowBreedError(mBreedTypeText, mBreedInputLayout, R.string.error_petbreed,
                    mPetTypeText.getText().toString());
            isValidGender = checkAndShowError(mPetGenderText, mGenderInputLayout, R.string.error_petgender);
            isValidWeight = checkAndShowError(mPetWeight, mWeightInputLayout, R.string.error_validweight);
            isValidAge = checkAndShowError(mPetAgeText, mAgeInputLayout, R.string.error_petage, mPetBirthdayText);

            if (isValidPetName ||
                    isValidOwnerName ||
                    isValidPetType ||
                    isValidBreedType ||
                    isValidGender ||
                    isValidWeight ||
                    isValidAge) {
                Utils.displayCrouton(getActivity(), getString(R.string.errorMsgForEmail), mContainerLayout);
                return super.onOptionsItemSelected(item);
            }
        }
        //Todo Remove all hardcoded value after api integration
        progressBar.setVisibility(View.VISIBLE);
        for (int i = 0; nameValueDetailsForMedicalallergy != null && i < nameValueDetailsForMedicalallergy.size();
                i++) {
            if (i < 4) {
                medicationAllergyArray[i] = nameValueDetailsForMedicalallergy.get(i).getName();
            } else {
                break;
            }
        }
        for (int i = 0;
                nameValueDetailsForCurrentMedications != null && i < nameValueDetailsForCurrentMedications.size();
                i++) {
            if (i < 4) {
                currentMedicationArray[i] = nameValueDetailsForCurrentMedications.get(i).getName();
            } else {
                break;
            }
        }
        if (petAgeData != null) {
            for (NameValueData ageData : petAgeData) {
                if (ageData.getName().equals(mPetAgeText.getText().toString())) {
                    age = ageData.getValue();
                    break;
                }
            }
        }
        if (isEditable) {
            //mPetBirthdayText.getText().toString();
            AddPetRequest addPetRequest = new AddPetRequest(mPet.getPetId(), mPetNameText.getText().toString()
                    , mOwnerNameText.getText().toString(),
                    mPetTypeText.getText().toString().toLowerCase(),
                    mBreedTypeText.getText().toString(),
                    mPetGenderText.getText().toString().toLowerCase(),
                    mPetWeight.getText().toString(),
                    age,
                    mPetBirthdayText.getText().toString(),
                    "yes",
                    "dog two allergy info",
                    medicationAllergyArray[MEDICATION_ALLERGY_1],
                    medicationAllergyArray[MEDICATION_ALLERGY_2],
                    medicationAllergyArray[MEDICATION_ALLERGY_3],
                    medicationAllergyArray[MEDICATION_ALLERGY_4],
                    medConditionIds,
                    "dog two other info",
                    currentMedicationArray[MEDICATION_ALLERGY_1],
                    currentMedicationArray[MEDICATION_ALLERGY_2],
                    currentMedicationArray[MEDICATION_ALLERGY_3],
                    currentMedicationArray[MEDICATION_ALLERGY_4],
                    mPreferencesHelper.getSessionConfirmationResponse().getSessionConfirmationNumber());
            mPresenter.updatePetData(addPetRequest);
        } else {

            AddPetRequest addPetRequest = new AddPetRequest(mPetNameText.getText().toString()
                    , mOwnerNameText.getText().toString(),
                    mPetTypeText.getText().toString().toLowerCase(),
                    mBreedTypeText.getText().toString(),
                    mPetGenderText.getText().toString().toLowerCase(),
                    mPetWeight.getText().toString(),
                    age,
                    Utils.changeDateFormat(birthdayInMillis, "MM/dd/yyyy"),
                    "yes",
                    "dog two allergy info",
                    medicationAllergyArray[MEDICATION_ALLERGY_1],
                    medicationAllergyArray[MEDICATION_ALLERGY_2],
                    medicationAllergyArray[MEDICATION_ALLERGY_3],
                    medicationAllergyArray[MEDICATION_ALLERGY_4],
                    medConditionIds,
                    "dog two other info",
                    currentMedicationArray[MEDICATION_ALLERGY_1],
                    currentMedicationArray[MEDICATION_ALLERGY_2],
                    currentMedicationArray[MEDICATION_ALLERGY_3],
                    currentMedicationArray[MEDICATION_ALLERGY_4],
                    mPreferencesHelper.getSessionConfirmationResponse().getSessionConfirmationNumber());
            mPresenter.addPetData(addPetRequest);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean checkAndShowError(EditText auditEditText, TextInputLayout auditTextInputLayout, int errorStringId) {
        if (auditEditText.getText().toString().isEmpty()) {
            auditTextInputLayout.setError(getContext().getString(errorStringId));
            return true;
        } else {
            auditTextInputLayout.setError(null);
            auditTextInputLayout.setErrorEnabled(false);
            return false;
        }
    }

    @Override
    public boolean isActive() {
        return isAdded();
    }

    @Override
    public void onUpdateSuccess(Pets pet) {
        //TODO Uncommet it when the reponse from the server is OK
        if (finalUri != null) {
            mPresenter.uploadPetImage(createPartFromString(pet.getPetId()), prepareFilePart("uploadfile", finalUri));
        } else {
            progressBar.setVisibility(View.GONE);
            closeWindow();
        }

    }

    private void closeWindow() {
        FragmentManager manager = getActivity().getSupportFragmentManager();
        FragmentTransaction trans = manager.beginTransaction();
        trans.remove(this);
        trans.commit();
        manager.popBackStack();
    }

    @Override
    public void onError(final String errorMessage) {
        progressBar.setVisibility(View.GONE);
        Utils.displayCrouton(getActivity(), errorMessage, mContainerLayout);

        //   Snackbar.make(mPetWeight, errorMessage, Snackbar.LENGTH_LONG).show();

    }

    public void showPetMedicalData(String title, List list) {
        //two activities HomeActivity and AddNewEntity are
        if (getActivity() instanceof HomeActivity) {
            ((HomeActivity) getActivity()).hideProgress();
        }
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.dailog_recyclerview_layout, null);
        ((TextView) view.findViewById(R.id.titleText)).setText(title);
        mAlertRecyclerViewAdapter = new AlertRecyclerViewAdapter(getActivity());
        mAlertRecyclerViewAdapter.setListData((ArrayList<AlertDailogMultipleChoice>) list);
        recyclerView = ((AlertRecyclerView) view.findViewById(R.id.recyclerView));
        recyclerView.setAlertRecyclerView(mAlertRecyclerViewAdapter);
        AlertDialog alertDialog = Utils
                .showAlertDailogListView(getActivity(), view,
                        R.style.StyleForNotification)
                .setCancelable(false)
                .setPositiveButton(getString(R.string.dialog_ok_button).toUpperCase(), this)
                .setNegativeButton(getString(R.string.dialog_cancel_button).toUpperCase(), this)
                .create();
        alertDialog.show();
    }

    private void executeCommonLogic(PetMedicationResponse response, String title,
            ArrayList<AlertDailogMultipleChoice> list, ArrayList<NameValueData> nameValuelist) {
        list = new ArrayList<AlertDailogMultipleChoice>();
        for (MedAllergy medication : response.getMedications()) {
            AlertDailogMultipleChoice alertDailogMultipleChoice = new AlertDailogMultipleChoice();
            alertDailogMultipleChoice.setName(medication.getName());
            alertDailogMultipleChoice.setValue(medication.getValue());
            if (isEditable && nameValuelist != null
                    && nameValuelist.size() > 0) {
                for (NameValueData data : nameValuelist) {
                    if (data.getValue().equalsIgnoreCase(alertDailogMultipleChoice.getValue())) {
                        alertDailogMultipleChoice.setChecked(true);
                    }

                }
            }
            list.add(alertDailogMultipleChoice);
        }
        showPetMedicalData(title, list);
    }

    @Override
    public void populateData(PetMedicationResponse response) {
        if (fromWhichDailog == IS_MEDICATIONS_ALLERGY_DAILOG) {
            executeCommonLogic(response, getString(R.string.title_for_pet_allergies), petMedicationAlleryList,
                    nameValueDetailsForMedicalallergy);
        } else {
            executeCommonLogic(response, getString(R.string.current_medication_title), petCurrentMedicationList,
                    nameValueDetailsForCurrentMedications);
        }

    }

    @Override
    public void populatePetAgeData(AgeListResponse response) {
        ArrayList<String> list = new ArrayList<String>();
        petAgeData = response.getAgeList();
        for (NameValueData data : petAgeData) {
            list.add(data.getName());
        }
        String dataArray[] = new String[list.size()];
        openDailog(list.toArray(dataArray), AGE_REQUEST, getActivity().getString(R.string.choose_range_txt));

    }

    @Override
    public void populatePetTypeData(PetTypesListResponse response) {
        ArrayList<String> list = new ArrayList<String>();
        for (NameValueData data : response.getPetTypes()) {
            list.add(data.getValue());
        }
        String dataArray[] = new String[list.size()];
        openDailog(list.toArray(dataArray), TYPE_REQUEST, getActivity().getString(R.string.choose_pet_title));
    }

    @Override
    public void populatePetBreedTypeData(PetBreedTypeListResponse response) {
        ArrayList<String> list = new ArrayList<String>();
        if (mSelectedPetType == null || mSelectedPetType.isEmpty()) {
            for (BreedItem data : response.getBreeds()) {
                list.add(data.getValue());
            }
        } else {
            for (BreedItem data : response.getBreeds()) {
                if (data.getType().equalsIgnoreCase(mSelectedPetType)) {
                    list.add(data.getValue());
                }
            }
        }
        String dataArray[] = new String[list.size()];
        if (dataArray.length > 0) {
            openDailog(list.toArray(dataArray), BREED_REQUEST, getActivity().getString(R.string.choose_breed_title));
        }
    }

    @Override
    public void populatePetMedicalconditionsData(PetMedicalConditionResponse response) {
        petMedicationConditionList = new ArrayList<AlertDailogMultipleChoice>();
        for (NameValueData valueData : response.getMedicalConditions()) {
            AlertDailogMultipleChoice alertDailogMultipleChoice = new AlertDailogMultipleChoice();
            alertDailogMultipleChoice.setName(valueData.getName());
            alertDailogMultipleChoice.setValue(valueData.getValue());
            if (isEditable && nameValueDetailsForMedicalConditons != null
                    && nameValueDetailsForMedicalConditons.size() > 0) {
                for (NameValueData data : nameValueDetailsForMedicalConditons) {
                    if (data.getValue().equalsIgnoreCase(alertDailogMultipleChoice.getValue())) {
                        alertDailogMultipleChoice.setChecked(true);
                    }
                }
            }
            petMedicationConditionList.add(alertDailogMultipleChoice);
        }
        showPetMedicalData(getString(R.string.title_for_pet_conditions), petMedicationConditionList);
    }

    @Override
    public void onPetAddSuccess(Pets pet) {
        mPets = pet;
        //TODO uncomment it when repsonse from server is OK
        if (finalUri != null) {
            mPresenter.uploadPetImage(createPartFromString(pet.getPetId()), prepareFilePart("uploadfile", finalUri));
        } else {
            onAddPetSuccess(mPets);
        }

    }

    private void onAddPetSuccess(Pets pet) {
        progressBar.setVisibility(View.GONE);
        closeWindow();
        if (mCallback != null) {
            mCallback.setPet(pet);
        }
        if (mAddPetNameListener != null) {
            mAddPetNameListener.openAddPetDailog(pet.getPetName());
        }
    }

    @Override
    public void onPetRemoved(int key) {
        progressBar.setVisibility(View.GONE);

        if(key == DialogOptionEnum.PET_PASSED.getValue()) {
            Snackbar.make(mPetWeight, getActivity().getString(R.string.pet_passed_removed_msg), Snackbar.LENGTH_LONG).show();
        }
        else {
            Snackbar.make(mPetWeight, getActivity().getString(R.string.pet_removed_msg), Snackbar.LENGTH_LONG).show();
        }

        closeWindow();
    }

    @Override
    public void onImageUploadSuccess() {
        if (isEditable) {
            progressBar.setVisibility(View.GONE);
            closeWindow();
        } else {
            onAddPetSuccess(mPets);
        }
    }

    @Override
    public void onImageUploadError(String error) {
        Utils.displayCrouton(getActivity(), getString(R.string.pet_image_upload_error), mContainerLayout);
    }

    @Override
    public boolean checkAndShowBreedError(EditText auditEditText, TextInputLayout auditTextInputLayout,
            int errorStringId, String petType) {
        if (petType.equalsIgnoreCase(getString(R.string.breed_type_cat)) || petType
                .equalsIgnoreCase(getString(R.string.breed_type_dog))) {
            if (auditEditText.getText().toString().isEmpty()) {
                auditTextInputLayout.setError(getContext().getString(errorStringId));
                return true;
            } else {
                auditTextInputLayout.setError(null);
                auditTextInputLayout.setErrorEnabled(false);
                return false;
            }
        } else {
            return false;
        }

    }

    public boolean checkAndShowError(EditText auditEditText, TextInputLayout auditTextInputLayout, int errorStringId,
            EditText birthdayText) {
        if (auditEditText.getText().toString().isEmpty() && birthdayText.getText().toString().isEmpty()) {
            auditTextInputLayout.setError(getContext().getString(errorStringId));
            return true;
        } else {
            auditTextInputLayout.setError(null);
            auditTextInputLayout.setErrorEnabled(false);
            return false;
        }
    }

    @Override
    public void setPresenter(AddPetContract.Presenter presenter) {

    }

    private void enableDoneAction() {
        mDoneMenuItem.setVisible(true);
        mEditMenuItem.setVisible(false);
    }

    private void enableEditAction() {
        mDoneMenuItem.setVisible(false);
        mEditMenuItem.setVisible(true);
    }

    private void enableViews(boolean isEnable) {
        mPetNameText.setEnabled(isEnable);
        mOwnerNameText.setEnabled(isEnable);
        mPetTypeText.setEnabled(isEnable);
        mBreedTypeText.setEnabled(isEnable);
        mPetGenderText.setEnabled(isEnable);
        mPetAgeText.setEnabled(isEnable);
        mPetWeight.setEnabled(isEnable);
        mPetBirthdayText.setEnabled(isEnable);
        removePetButton.setEnabled(isEnable);
        mEditPetImage.setEnabled(isEnable);
        //  mPetAgeText.setEnabled(isEnable);
        mAddEditMedicationAllergies.setEnabled(isEnable);
        mAddEditMedicationConditions.setEnabled(isEnable);
        mAddEditCurrentMedications.setEnabled(isEnable);
    }

    private void setPetData(Pets pet) {
        if (pet != null) {
            mPetNameText.setText(pet.getPetName());
            mOwnerNameText.setText(pet.getOwnerName());
            mPetTypeText.setText(pet.getPetType());
            mBreedTypeText.setText(pet.getBreedType());
            mPetGenderText.setText(pet.getGender());
            mPetWeight.setText(pet.getWeight());
            mPetBirthdayText.setText(Utils.changeStringtoMillis(pet.getBirthday()));

            if (pet.getPetAge() != null) {
                age = pet.getPetAge().getValue();
                mPetAgeText.setText(pet.getPetAge().getName());
            }

            updateAllergiesDetails(pet, IS_MEDICATIONS_ALLERGY_DAILOG);
            updateAllergiesDetails(pet, IS_MEDICATIONS_CONDITIONS_DAILOG);
            updateAllergiesDetails(pet, IS_CURRENT_MEDICATIONS_DAILOG);
            Glide.with(getActivity()).load(getActivity().getString(R.string.server_endpoint) + pet.getPictureURL())
                    .asBitmap().centerCrop().into(new BitmapImageViewTarget(mEditPetImage) {
                @Override
                protected void setResource(Bitmap resource) {
                    RoundedBitmapDrawable circularBitmapDrawable =
                            RoundedBitmapDrawableFactory.create(getActivity().getResources(), resource);
                    circularBitmapDrawable.setCircular(true);
                    mEditPetImage.setImageDrawable(circularBitmapDrawable);
                }
            });

        }
    }

    public void updateTextValues(TextView button_text, TextView details_text, int size, String value) {
        if (size > 0) {
            button_text.setText(getString(R.string.edit));
            details_text.setVisibility(View.VISIBLE);
            details_text.setText(value);
        } else {
            details_text.setVisibility(View.GONE);
            button_text.setText(getString(R.string.add_title));
        }
    }

    public void updateValues(int fromPicker, int size, String value, ArrayList<NameValueData> nameValueList) {
        switch (fromPicker) {
            case IS_MEDICATIONS_ALLERGY_DAILOG:
                updateTextValues(mAddEditMedicationAllergies, mMedicationAllergiesDetails, size, value);
                nameValueDetailsForMedicalallergy = nameValueList;
                break;
            case IS_CURRENT_MEDICATIONS_DAILOG:
                updateTextValues(mAddEditCurrentMedications, mCurrentMedicationsDetails, size, value);
                nameValueDetailsForCurrentMedications = nameValueList;
                break;
            case IS_MEDICATIONS_CONDITIONS_DAILOG:
                updateTextValues(mAddEditMedicationConditions, mMedicationConditionsDetails, size, value);
                updateConditionIds(nameValueList);
                nameValueDetailsForMedicalConditons = nameValueList;
                break;
        }
    }

    public void updateConditionIds(ArrayList<NameValueData> details) {
        medConditionIds = new ArrayList<Integer>();
        for (NameValueData detail : details) {
            medConditionIds.add(Integer.valueOf(detail.getValue()));
        }
    }

    public void updateAllergiesDetails(Pets pet, int fromPicker) {
        String value = "";
        ArrayList<NameValueData> list;

        if (isEditable && pet != null && fromPicker == IS_MEDICATIONS_ALLERGY_DAILOG) {
            list = pet.getMedAllergies();
        } else if (isEditable && pet != null && fromPicker == IS_MEDICATIONS_CONDITIONS_DAILOG) {
            list = pet.getMedConditions();
        } else if (isEditable && pet != null && fromPicker == IS_CURRENT_MEDICATIONS_DAILOG) {
            list = pet.getMedications();
        } else {
            list = mAlertRecyclerViewAdapter.getCheckedItems();
            populateMedicationLists(fromPicker);
        }
        int size = list.size();
        for (int i = 0; i < size; i++) {
            value = value + ((size - 1 == 0 || size - 1 == i) ? (list.get(i).getName())
                    : (list.get(i).getName() + NEXT_LINE));
        }
        updateValues(fromPicker, size, value, list);
    }

    private void populateMedicationLists(int fromPicker) {
        switch (fromPicker) {
            case IS_MEDICATIONS_ALLERGY_DAILOG:
                petMedicationAlleryList = mAlertRecyclerViewAdapter.getItems();
                break;
            case IS_MEDICATIONS_CONDITIONS_DAILOG:
                petMedicationConditionList = mAlertRecyclerViewAdapter.getItems();
                break;
            case IS_CURRENT_MEDICATIONS_DAILOG:
                petCurrentMedicationList = mAlertRecyclerViewAdapter.getItems();
                break;

        }
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        switch (which) {
            case DialogInterface.BUTTON_POSITIVE:
                updateAllergiesDetails(null, fromWhichDailog);
                break;
            case DialogInterface.BUTTON_NEGATIVE:

                break;

        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mAlertRecyclerViewAdapter = null;
        recyclerView = null;

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            if (context instanceof AddNewEntityActivity) {
                mCallback = (AddNewEntityActivity) context;
            }
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement AddACardContract.AddressSelectionListener");
        }

    }

    @NonNull
    private RequestBody createPartFromString(String descriptionString) {
        return RequestBody.create(
                MediaType.parse("text/plain"), descriptionString);
    }

    @NonNull
    private MultipartBody.Part prepareFilePart(String partName, Uri fileUri) {
        // https://github.com/iPaulPro/aFileChooser/blob/master/aFileChooser/src/com/ipaulpro/afilechooser/utils/FileUtils.java
        // use the FileUtils to get the actual file by uri
        File file = new File(fileUri.getPath());
        // create RequestBody instance from file
        RequestBody requestFile =
                RequestBody.create(MediaType.parse("image/*"), file);

        // MultipartBody.Part is used to send also the actual file name
        MultipartBody.Part body =
                MultipartBody.Part.createFormData("uploadfile", file.getName(), requestFile);
        return body;
    }


}
