# GeoTask
Simple application, which demonstrates usage of Google Maps, Places API and Directions API

## Used libraries:
Retrofit2, Gson

Google Play Services: Maps, Places, Locations

Appcompat-v7, Design library

App consists of three activities: 
#### 1) Splash Screen

#### 2) Activity with Tabs - where User must select Location From and Location To.

Every tab consists of Autocomplete Fragment, List of recent searches (visible 3 item, else - scrolable) and map with marker. 

![1](https://cloud.githubusercontent.com/assets/26281027/25373110/040cb734-29a1-11e7-884e-5b9a573c1c69.png)
![2](https://cloud.githubusercontent.com/assets/26281027/25373113/0410e714-29a1-11e7-9196-9b05de2ef86a.png)
![3](https://cloud.githubusercontent.com/assets/26281027/25373111/040d7caa-29a1-11e7-9c25-07b2352a9eb9.png)

#### 3) Map Result Activity.

Map contains markers from and to and shows current location.

Retrofit client get request from Google Directions API, GSon converts json-answer and writes coordinates for the route between two markers.
Coordinates get's together in Polyline.

![4](https://cloud.githubusercontent.com/assets/26281027/25373112/040fd25c-29a1-11e7-9dd2-24ce8445c329.png)
