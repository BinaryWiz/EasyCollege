# Easy College

An application to allow people to make college searching simpler with just two clicks

# Goals for the App

* Wanted to create a centralized place to get basic, but essential information about a college, like the location, net price, 
SAT and ACT ranges, and the acceptance rate of the college

* Save the college in the application for easy reference later

# How it Was Made

* Used AWS Lambda functions for information retrieval 
([getCollegeOptionsLambda](https://github.com/BinaryWiz/GetCollegeOptionsLambda) and 
[getCollegeInformation](https://github.com/BinaryWiz/GetCollegeInformation)

* The actual application is pretty much a ScrollView of the the Card objects that contain the college information

<img src="http://timeless-apps.com/img/easy_college_screenshot.png" width="250">
