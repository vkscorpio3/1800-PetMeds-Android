package com.petmeds1800.ui.pushnotifications;

import com.petmeds1800.model.entities.PushNotificationRequest;
import com.petmeds1800.mvp.BasePresenter;

/**
 * Created by Sdixit on 15-12-2016.
 */

public interface PushNotificationContract {

    interface View {
        void onNotificationFlagSuccess();
        void onNotificationFlagError(String error);
        void onError(String error);
        boolean isActive();
    }

    interface Presenter extends BasePresenter {
        void savePushNotificationFlag(PushNotificationRequest pushNotificationRequest);
    }

}
