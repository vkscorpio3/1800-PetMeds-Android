package com.petmeds1800.dagger.module;

import com.petmeds1800.ui.fragments.LoginFragment;

/**
 * Specifies the injection places. Utility interface, to separate from the {@link AppComponent}.
 *
 * @author Konrad
 */
public interface Injector {

    void inject(LoginFragment loginFragment);
}
