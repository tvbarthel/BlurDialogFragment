BlurDialogFragment
==================

This project allows to display DialogFragment with a burring effect behind. The blurring part is achieved through FastBlur algorithm thanks to the impressive work of Pavlo Dudka (cf [Special Thanks](https://github.com/tvbarthel/BlurDialogFragment/#special-thanks-to-)). 

[![Android Arsenal](https://img.shields.io/badge/Android%20Arsenal-BlurDialogFragment-brightgreen.svg?style=flat)](https://android-arsenal.com/details/1/1064)

* [Sample app](#sample-app)
* [Example](#example)
* [Dependency](#dependency)
* [Simple usage using inheritance](#simple-usage-using-inheritance)
* [Customize your blurring effect](#customize-your-blurring-effect)
* [Avoiding inheritance](#avoiding-inheritance)
* [Benchmark](#benchmark)
* [Known bugs](#known-bugs)
* [RenderScript or not RenderScript](#renderscript-or-not-renderscript)
* [TODO](#todo)
* [Change logs](#change-logs)
* [Contributing](#contributing)
* [Credits](#credits)
* [License](#license)
* [Special Thanks](#special-thanks-to-)

Sample app
=======
[Download the sample app on the Google Play store.](https://play.google.com/store/apps/details?id=fr.tvbarthel.lib.blurdialogfragment.sample)

Example
=======
Activity with action bar [blurRadius 4, downScaleFactor 5.0] : 
![action bar blur](/static/action_bar_blur.png)

Fullscreen activity [blurRadius 2, downScaleFactor 8.0] : 
![full screen blur](/static/full_screen_blur.png)

Dependency
=======
In order to use this library, just add a new gradle dependency : [BlurDialogFragment dependency](https://github.com/tvbarthel/maven#usage) 

Simple usage using inheritance
=======
If you are using **android.app.DialogFragment** : extends **BlurDialogFragment**. 
Play with the blur radius and the down scale factor to obtain the perfect blur.

Don't forget to enable log if you want to keep on eye the perfomance.

```java
/**
 * Simple fragment with blurring effect behind.
 */
public class SampleDialogFragment extends BlurDialogFragment {

}
```

If you are using **android.support.v4.app.DialogFragment** : extends **SupportBlurDialogFragment**. 
Play with the blur radius and the down scale factor to obtain the perfect blur.

Don't forget to enable log in order to keep on eye the perfomance.

```java
/**
 * Simple fragment with blurring effect behind.
 */
public class SampleDialogFragment extends SupportBlurDialogFragment {

}
```

Customize your blurring effect
======
```java

/**
 * Simple fragment with a customized blurring effect.
 */
public class SampleDialogFragment extends BlurDialogFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        ...
    }
    
    @Override
    protected float getDownScaleFactor() {
        // Allow to customize the down scale factor.
        return 5.0;
    }

    @Override
    protected int getBlurRadius() {
        // Allow to customize the blur radius factor.
        return 7;
    }
    
    @Override
    protected boolean isActionBarBlurred() {
        // Enable or disable the blur effect on the action bar.
        // Disabled by default.
        return true;
    }
    
    @Override
    protected boolean isDimmingEnable() {
        // Enable or disable the dimming effect.
        // Disabled by default.
        return true;
    }

    @Override
    protected boolean isRenderScriptEnable() {
        // Enable or disable the use of RenderScript for blurring effect
        // Disabled by default.
        return true;
    }
    
    @Override
    protected boolean isDebugEnable() {
        // Enable or disable debug mode.
        // False by default.
        return true;
    }
    ...
```

Default values are set to : 
 ```java
 
    /**
     * Since image is going to be blurred, we don't care about resolution.
     * Down scale factor to reduce blurring time and memory allocation.
     */
    static final float DEFAULT_BLUR_DOWN_SCALE_FACTOR = 4.0f;

    /**
     * Radius used to blur the background
     */
    static final int DEFAULT_BLUR_RADIUS = 8;

    /**
     * Default dimming policy.
     */
    static final boolean DEFAULT_DIMMING_POLICY = false;

    /**
     * Default debug policy.
     */
    static final boolean DEFAULT_DEBUG_POLICY = false;

    /**
     * Default action bar blurred policy.
     */
    static final boolean DEFAULT_ACTION_BAR_BLUR = false;

    /**
     * Default use of RenderScript.
     */
    static final boolean DEFAULT_USE_RENDERSCRIPT = false;
    
```

Avoiding inheritance
=======

If you want to **avoid inheritance**, use directly the **BlurEngine**. 
Don't forget to link the engine to the lifecycle of your DialogFragment.

```java
/**
 * Your blur fragment directly using BlurEngine.
 */
public class SampleDialogFragment extends MyCustomDialogFragment {

     /**
     * Engine used to blur.
     */
    private BlurDialogEngine mBlurEngine;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        mBlurEngine = new BlurDialogEngine(getActivity());
        mBlurEngine.debug(mDebugEnable);
        mBlurEngine.setBlurRadius(8);
        mBlurEngine.setDownScaleFactor(8f);
        mBlurEngine.debug(true);
        mBlurEngine.setBlurActionBar(true);
        mBlurEngine.setUseRenderScript(true);
    }
    
    @Override
    public void onResume() {
        super.onResume();
        mBlurEngine.onResume(getRetainInstance());
    }
    
     @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        mBlurEngine.onDismiss();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mBlurEngine.onDestroy();
    }

    @Override
    public void onDestroyView() {
        if (getDialog() != null) {
            getDialog().setDismissMessage(null);
        }
        super.onDestroyView();
    }
    
    ...
}
```

Benchmark
=======

We used a Nexus 5 running a 4.4.4 stock rom for this bench.

Down scale factor 8.0 & Blur Radius 8 : [Screenshot](/static/blur_8.0_8.png)
```javascript
Radius : 8
Down Scale Factor : 8.0
Blurred achieved in : 18 ms
Allocation : 4320ko (screen capture) + 270ko (FastBlur)
```

Down scale factor 6.0 & Blur Radius 12 : [Screenshot](/static/blur_6.0_12.png)
```javascript
Radius : 12
Down Scale Factor : 6.0
Blurred achieved in : 31 ms
Allocation : 4320ko (screen capture) + 360ko (FastBlur)
```

Down scale factor 4.0 & Blur Radius 20 : [Screenshot](/static/blur_4.0_20.png)
```javascript
Radius : 20
Down Scale Factor : 4.0
Blurred achieved in : 75 ms
Allocation : 4320ko (screen capture) + 540ko (FastBlur)
```


Known bugs
=======
* Wrong top offset when using the following line in application Theme :
```xml
<item name="android:windowActionBarOverlay">true</item>
```

RenderScript or not RenderScript
=======
Thanks to [amasciul](https://github.com/amasciul) blurring effect can now be achieved using ScriptIntrinsicBlur (v1.1.0).

Find more information on the [memory trace](http://tvbarthel.github.io/blur-dialog-fragment.html) and on the [execution time](http://trickyandroid.com/advanced-blurring-techniques/#comment-1557039595).



TODO
=======
* Implement SherlockBlurDialogFragment since actionbarsherlock and appcompat are mutually exclusive.

Change logs
=======
* 1.1.0 : Allow to use RenderScript (thank to [amasciul](https://github.com/amasciul)).
* 1.0.0 : Animate blurring effect, support tablet, tweak nav bar offset and reduce memory allocation.
* 0.1.2 : Fix bottom offset introduce by the navigation bar on Lollipop.
* 0.1.1 : Fix top offset when using Toolbar.
* 0.1.0 : Support appcompat-v7:21.
* 0.0.9 : Change default blur radius (8) and default down scale factor (4).
* 0.0.8 : Fix NoClassDefFound.
* 0.0.7 : Avoid using inheritance through BlurDialogEngine if needed.

Contributing
=======

Contributions are welcome (: You can contribute through GitHub by forking the repository and sending a pull request.

When submitting code, please make every effort to follow existing conventions and style in order to keep the code as readable as possible. Please also make sure your code fit these convention by running gradlew check.

Credits
========
Credits go to Thomas Barthélémy [https://github.com/tbarthel-fr](https://github.com/tbarthel-fr) and Vincent Barthélémy [https://github.com/vbarthel-fr](https://github.com/vbarthel-fr).

License
=====================
Copyright (C) 2014 tvbarthel

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

Special Thanks to ...
========
Pavlo Dudka [https://github.com/paveldudka/](https://github.com/paveldudka/) , for his impressive article on [Advanced blurring techniques](http://trickyandroid.com/advanced-blurring-techniques/).

Vincent Brison [https://github.com/vincentbrison](https://github.com/vincentbrison) , for his early day support.

Alexandre Masciulli [https://github.com/amasciul](https://github.com/amasciul) , for the integration of RenderScript.
