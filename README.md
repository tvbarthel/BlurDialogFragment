BlurDialogFragment
==================

This project allows to display DialogFragment with a burring effect behind. The blurring part is achieved through FastBlur algorithm thanks to the impressive work of Pavlo Dudka (cf [Special Thanks](https://github.com/tvbarthel/BlurDialogFragment/#special-thanks-to-)). 

This project is based on android.support.v4.app.DialogFragment and android.support.v7.app.ActionBarActivity.

* [Example](https://github.com/tvbarthel/BlurDialogFragment/#example)
* [Dependency](https://github.com/tvbarthel/BlurDialogFragment/#dependency)
* [Simple usage](https://github.com/tvbarthel/BlurDialogFragment/#simple-usage)
* [Known bugs](https://github.com/tvbarthel/BlurDialogFragment/#known-bugs)
* [TODO](https://github.com/tvbarthel/BlurDialogFragment/#todo)
* [Credits](https://github.com/tvbarthel/BlurDialogFragment/#credits)
* [License](https://github.com/tvbarthel/BlurDialogFragment/#license)
* [Special Thanks](https://github.com/tvbarthel/BlurDialogFragment/#special-thanks-to-)

Example
=======
Activity with action bar [blurRadius 4, downScaleFactor 5.0] : 
![action bar blur](/static/action_bar_blur.png)

Fullscreen activity [blurRadius 2, downScaleFactor 8.0] : 
![full screen blur](/static/full_screen_blur.png)

Dependency
=======
In order to use this library, just add a new gradle dependency : [BlurDialogFragment dependency](https://github.com/tvbarthel/maven#usage) 

Simple Usage
=======

Extends BlurDialogFragment. Play with the blur radius and the down scale factor to obtain the perfect blur.

Don't forget to enable log in order to keep on eye the perfomance.

```java
/**
 * Simple fragment with blurring effect behind.
 */
public class SampleDialogFragment extends BlurDialogFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.enableLog(true);
        this.setBlurRadius(4);
        this.setDownScaleFactor(5.0f);
        
        ...
    }
    
    ...
}
```

Default values are set to : 
 ```java
    /**
     * Since image is going to be blurred, we don't care about resolution.
     * Down scale factor reduces blurring time and memory allocation.
     */
    private static final float BLUR_DOWN_SCALE_FACTOR = 8.0f;

    /**
     * Radius used to blur the background.
     */
    private static final int BLUR_RADIUS = 2;
    
```

Known bugs
=======
* Wrong top offset when using the following line in application Theme :
```xml
<item name="android:windowActionBarOverlay">true</item>
```

TODO
=======
* Implement SherlockBlurDialogFragment since actionbarsherlock and appcompat are mutually exclusive.
* Try to base blur on android.support.v8.renderscript to avoid copying entire bitmap.

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
