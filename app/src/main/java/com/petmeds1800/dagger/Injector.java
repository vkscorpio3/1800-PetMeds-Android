package com.petmeds1800.dagger;

import com.petmeds1800.api.RxCallAdapterFactoryWithErrorHandling;
import com.petmeds1800.dagger.component.AppComponent;
import com.petmeds1800.mvp.RefillNotificationPresenter;
import com.petmeds1800.mvp.SignupTask.SignUpPresenter;
import com.petmeds1800.mvp.contactustask.ContactUsPresenter;
import com.petmeds1800.service.RefillReminderService;
import com.petmeds1800.ui.HomeActivity;
import com.petmeds1800.ui.IntroActivity;
import com.petmeds1800.ui.ResetPasswordActivity;
import com.petmeds1800.ui.SplashActivity;
import com.petmeds1800.ui.account.AccountPresenter;
import com.petmeds1800.ui.account.AccountSettingsFragment;
import com.petmeds1800.ui.account.AccountSettingsPresenter;
import com.petmeds1800.ui.address.AddAddressPresenter;
import com.petmeds1800.ui.address.AddEditAddressFragment;
import com.petmeds1800.ui.address.SavedAddressListPresenter;
import com.petmeds1800.ui.checkout.CheckOutActivity;
import com.petmeds1800.ui.checkout.CheckoutActivityPresenter;
import com.petmeds1800.ui.checkout.StepTwoPresenter;
import com.petmeds1800.ui.checkout.StepTwoRootFragment;
import com.petmeds1800.ui.checkout.stepfive.StepFiveRootFragment;
import com.petmeds1800.ui.checkout.stepfive.StepFiveRootPresentor;
import com.petmeds1800.ui.checkout.stepfour.StepFourRootFragment;
import com.petmeds1800.ui.checkout.stepfour.presenter.PetVetInfoPresenter;
import com.petmeds1800.ui.checkout.stepfour.presenter.StepFourRootPresenter;
import com.petmeds1800.ui.checkout.steponerootfragment.GuestStepOneRootPresentor;
import com.petmeds1800.ui.checkout.steponerootfragment.StepOneRootFragment;
import com.petmeds1800.ui.checkout.steponerootfragment.StepOneRootPresentor;
import com.petmeds1800.ui.checkout.stepthreefragment.AddGuestCardFragment;
import com.petmeds1800.ui.checkout.stepthreefragment.GuestStepThreePresenter;
import com.petmeds1800.ui.checkout.stepthreefragment.GuestStepThreeRootFragment;
import com.petmeds1800.ui.checkout.stepthreefragment.PaymentSelectionListFragment;
import com.petmeds1800.ui.checkout.stepthreefragment.StepThreeRootFragment;
import com.petmeds1800.ui.checkout.stepthreefragment.StepThreeRootPresentor;
import com.petmeds1800.ui.dashboard.CategoryListFragment;
import com.petmeds1800.ui.dashboard.ProductCategoryPresenter;
import com.petmeds1800.ui.dashboard.WidgetListFragment;
import com.petmeds1800.ui.dashboard.presenter.WidgetPresenter;
import com.petmeds1800.ui.fragments.AccountFragment;
import com.petmeds1800.ui.fragments.AccountRootFragment;
import com.petmeds1800.ui.fragments.CartFragment;
import com.petmeds1800.ui.fragments.CommonWebviewFragment;
import com.petmeds1800.ui.fragments.ForgotPasswordFragment;
import com.petmeds1800.ui.fragments.HomeRootFragment;
import com.petmeds1800.ui.fragments.LoginFragment;
import com.petmeds1800.ui.fragments.SignOutFragment;
import com.petmeds1800.ui.fragments.SignUpFragment;
import com.petmeds1800.ui.fragments.dialog.FingerprintAuthenticationDialog;
import com.petmeds1800.ui.learn.FeaturedFragment;
import com.petmeds1800.ui.learn.MedConditionsFragment;
import com.petmeds1800.ui.learn.MedConditionsListPresenter;
import com.petmeds1800.ui.medicationreminders.AddEditMedicationRemindersFragment;
import com.petmeds1800.ui.medicationreminders.AddEditMedicationRemindersPresentor;
import com.petmeds1800.ui.medicationreminders.MedicationReminderItemsListPresentor;
import com.petmeds1800.ui.medicationreminders.MedicationReminderListPresentor;
import com.petmeds1800.ui.medicationreminders.service.UpdateMedicationRemindersAlarmService;
import com.petmeds1800.ui.orders.OrderDetailFragment;
import com.petmeds1800.ui.orders.presenter.OrderDetailPresenter;
import com.petmeds1800.ui.payment.AddACardPresenter;
import com.petmeds1800.ui.payment.AddEditCardFragment;
import com.petmeds1800.ui.payment.SavedCardsListPresenter;
import com.petmeds1800.ui.pets.AddPetFragment;
import com.petmeds1800.ui.pets.presenter.AddPetPresenter;
import com.petmeds1800.ui.pets.presenter.PetListPresenter;
import com.petmeds1800.ui.pushnotifications.PushNotificationPresenter;
import com.petmeds1800.ui.refillreminder.presenter.EditReminderPresenter;
import com.petmeds1800.ui.refillreminder.presenter.ReminderListPresenter;
import com.petmeds1800.ui.resetpassword.ResetPasswordFragment;
import com.petmeds1800.ui.resetpassword.ResetPasswordPresenter;
import com.petmeds1800.ui.shoppingcart.presenter.ShoppingCartListPresenter;
import com.petmeds1800.ui.support.SecurityLockPresenter;
import com.petmeds1800.ui.vet.AddVetFragment;
import com.petmeds1800.ui.vet.AddVetPresenter;
import com.petmeds1800.ui.vet.CantFindVetFragment;
import com.petmeds1800.ui.vet.CantFindVetPresenter;
import com.petmeds1800.ui.vet.EditVetFragment;
import com.petmeds1800.ui.vet.VetDetailFragment;
import com.petmeds1800.ui.vet.VetListPresenter;
import com.petmeds1800.ui.vet.presenter.EditVetPresenter;
import com.petmeds1800.ui.vet.presenter.FindVetPresenter;
import com.petmeds1800.ui.vet.presenter.VetDetailPresenter;
import com.petmeds1800.util.AnalyticsUtil;
import com.petmeds1800.util.BootReceiver;
import com.petmeds1800.util.PetMedWebViewClient;

/**
 * Specifies the injection places. Utility interface, to separate from the {@link AppComponent}.
 *
 * @author Konrad
 */
public interface Injector {

    void inject(SplashActivity splashActivity);

    void inject(HomeActivity homeActivity);

    void inject(LoginFragment loginFragment);

    void inject(ForgotPasswordFragment forgotPasswordFragment);

    void inject(FingerprintAuthenticationDialog fingerprintAuthenticationDialog);

    void inject(IntroActivity introActivity);

    void inject(SignUpFragment signUpFragment);

    void inject(SignUpPresenter signUpPresenter);

    void inject(StepTwoPresenter stepTwoPresenter);

    void inject(AddACardPresenter addACardPresenter);

    void inject(SavedCardsListPresenter savedCardsListPresenter);

    void inject(AccountSettingsPresenter accountSettingsPresenter);

    void inject(SavedAddressListPresenter savedAddressListPresenter);

    void inject(AddAddressPresenter addAddressPresenter);

    void inject(AddEditCardFragment addEditCardFragment);

    void inject(AddEditAddressFragment addEditAddressFragment);

    void inject(AccountSettingsFragment accountSettingsFragment);

    void inject(PetListPresenter petsListPresenter);

    void inject(AddPetPresenter addPetPresenter);

    void inject(AddPetFragment addPetFragment);

    void inject(AccountFragment accountFragment);

    void inject(SignOutFragment signoutfragment);

    void inject(AccountRootFragment accountRootFragment);

    void inject(RefillReminderService refillReminderService);

    void inject(AccountPresenter accountPresenter);

    void inject(AnalyticsUtil analyticsUtil);

    void inject(WidgetPresenter widgetPresenter);

    void inject(CategoryListFragment categoryListFragment);

    void inject(ProductCategoryPresenter productCategoryPresenter);

    void inject(ShoppingCartListPresenter shoppingCartListPresenter);

    void inject(CheckoutActivityPresenter widgetPresenter);

    void inject(StepOneRootPresentor stepOneRootPresentor);

    void inject(StepOneRootFragment stepOneRootFragment);

    void inject(StepTwoRootFragment stepTwoRootFragment);

    void inject(StepThreeRootFragment stepThreeRootFragment);

    void inject(PetVetInfoPresenter petVetInfoPresenter);

    void inject(StepFourRootPresenter stepFourRootPresenter);

    void inject(StepFourRootFragment stepFourRootFragment);

    void inject(StepFiveRootPresentor stepFiveRootPresentor);

    void inject(StepFiveRootFragment stepFiveRootFragment);

    void inject(StepThreeRootPresentor stepThreeRootPresentor);

    void inject(CantFindVetPresenter cantFindVetPresenter);

    void inject(CantFindVetFragment cantFindVetFragment);

    void inject(AddVetPresenter addVetPresenter);

    void inject(AddVetFragment addVetFragment);

    void inject(OrderDetailPresenter orderDetailPresenter);

    void inject(OrderDetailFragment orderDetailFragment);

    void inject(WidgetListFragment widgetListFragment);

    void inject(GuestStepOneRootPresentor guestStepOneRootPresentor);


    void inject(VetListPresenter vetListPresenter);

    void inject(MedicationReminderListPresentor medicationReminderListPresentor);

    void inject(CommonWebviewFragment commonWebviewFragment);

    void inject(GuestStepThreeRootFragment guestStepThreeRootFragment);

    void inject(GuestStepThreePresenter guestStepThreePresenter);

    void inject(AddGuestCardFragment addGuestCardFragment);

    void inject(FindVetPresenter findVetPresenter);

    void inject(EditVetPresenter editVetPresenter);

    void inject(EditVetFragment editVetFragment);

    void inject(CheckOutActivity addGuestCardFragment);

    void inject(MedConditionsListPresenter medConditionsListPresenter);

    void inject(VetDetailPresenter vetDetailPresenter);

    void inject(VetDetailFragment vetDetailFragment);

    void inject(PaymentSelectionListFragment paymentSelectionListFragment);


    void inject(AddEditMedicationRemindersPresentor addEditMedicationRemindersPresentor);

    void inject(AddEditMedicationRemindersFragment addEditMedicationRemindersFragment);

    void inject(MedicationReminderItemsListPresentor medicationReminderItemsListPresentor);

    void inject(UpdateMedicationRemindersAlarmService updateMedicationRemindersAlarmService);

    void inject(BootReceiver bootReceiver);

    void inject(ReminderListPresenter reminderListPresenter);

    void inject(EditReminderPresenter editReminderPresenter);

    void inject(CartFragment cartFragment);

    void inject(HomeRootFragment homeRootFragment);

    void inject(PetMedWebViewClient petMedWebViewClient);

    void inject(RefillNotificationPresenter refillNotificationPresenter);

    void inject(RxCallAdapterFactoryWithErrorHandling rxCallAdapterFactoryWithErrorHandling);

    void inject(PushNotificationPresenter pushNotificationPresenter);

    void inject(SecurityLockPresenter securityLockPresenter);

    void inject(MedConditionsFragment medConditionsFragment);

    void inject(FeaturedFragment featuredFragment);

    void inject(ContactUsPresenter contactUsPresenter);

    void inject(ResetPasswordPresenter resetPasswordPresenter);

    void inject(ResetPasswordFragment resetPasswordFragment);

    void inject(ResetPasswordActivity resetPasswordActivity);


}

