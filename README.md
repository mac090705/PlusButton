# PlusButton
Change the plus icon to tick or cross with animation when click the button
##Preview
![](https://github.com/mac090705/PlusButton/blob/master/screenshots/plusbutton.gif)
##Usage
```xml
<com.leo.plusbutton.PlusButton
    android:layout_width="150dp"
    android:layout_height="60dp"
    android:layout_margin="5dp"
    app:colorGradient="true"
    app:startColor="#0000ff"
    app:endColor="#ffffff"
    app:mode="hook"/>
```
```Java
plusButton.setOnClickListener(new PlusButton.OnClickListener() {
        @Override
        public void onFirstClick() {

        }

        @Override
        public void onConfirmClick() {

        }
    });
```
