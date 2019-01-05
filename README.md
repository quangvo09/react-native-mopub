
# react-native-mopub

## Getting started

`$ npm install react-native-mopub --save`

### Mostly automatic installation

`$ react-native link react-native-mopub`

### Manual installation


#### iOS

1. In XCode, in the project navigator, right click `Libraries` ➜ `Add Files to [your project's name]`
2. Go to `node_modules` ➜ `react-native-mopub` and add `RNMopub.xcodeproj`
3. In XCode, in the project navigator, select your project. Add `libRNMopub.a` to your project's `Build Phases` ➜ `Link Binary With Libraries`
4. Run your project (`Cmd+R`)<

#### Android

1. Open up `android/app/src/main/java/[...]/MainActivity.java`
  - Add `import com.smartapp.rnmopub.RNMopubPackage;` to the imports at the top of the file
  - Add `new RNMopubPackage()` to the list returned by the `getPackages()` method
2. Append the following lines to `android/settings.gradle`:
  	```
  	include ':react-native-mopub'
  	project(':react-native-mopub').projectDir = new File(rootProject.projectDir, 	'../node_modules/react-native-mopub/android')
  	```
3. Insert the following lines inside the dependencies block in `android/app/build.gradle`:
  	```
      compile project(':react-native-mopub')
  	```


## Usage
```javascript
import RNMopub from 'react-native-mopub';

// TODO: What to do with the module?
RNMopub;
```
  