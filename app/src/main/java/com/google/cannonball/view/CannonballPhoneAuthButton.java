/**
 * Copyright (C) 2017 Google Inc and other contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.cannonball.view;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.AppCompatButton;
import android.util.AttributeSet;

import com.google.cannonball.App;
import com.google.cannonball.R;

public class CannonballPhoneAuthButton extends AppCompatButton {
    public CannonballPhoneAuthButton(Context context) {
        this(context, null);
        init();
    }

    public CannonballPhoneAuthButton(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.buttonStyle);
        init();
    }

    public CannonballPhoneAuthButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        if (isInEditMode()){
            return;
        }
        final Drawable phone = getResources().getDrawable(R.drawable.ic_signin_phone);
        phone.setColorFilter(getResources().getColor(R.color.green), PorterDuff.Mode.SRC_ATOP);
        setCompoundDrawablesWithIntrinsicBounds(phone, null, null, null);
        setBackgroundResource(R.drawable.phone_auth_button);
        setTextSize(20);
        setTextColor(getResources().getColor(R.color.green));
        setTypeface(App.getInstance().getTypeface());
    }
}
