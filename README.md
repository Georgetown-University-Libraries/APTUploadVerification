# APTUploadVerification
Command Line Interface to the APTrust API: https://sites.google.com/a/aptrust.org/member-wiki/member-api/api-basics

![image](https://cloud.githubusercontent.com/assets/1111057/18217851/f5cc8a8c-7113-11e6-81ae-1658c270abbf.png)

## Demonstration Video
This video demonstrates the workflows developed by the Georgetown University Library to export content to Academic Preservation Trust.

[![Demonstration Video](https://i.ytimg.com/vi/SiMlY0g9cn0/hqdefault.jpg)](https://youtu.be/SiMlY0g9cn0)

## Overview Presentation
[APT Georgetown Bagging Strategy](https://docs.google.com/presentation/d/e/2PACX-1vQa6qObo617BtslVo6AxutjZ6duHyp0UluFuURw5_sHIHTDP54KH1t8nCcjayn9CAXRNyj-TkwE5Xc5/pub?start=false&loop=false&delayms=3000)

## Configuration File Format

    url = http://test.aptrust.org/member-api/v1 
    user = <user id for APTrust API>
    key = <API key for APTrust API>
    
## Build

    mvn clean package
    
## Execution
    >>>>java -jar target\APTUploadVerification-1.0.jar -h
    AptQuery [-apiprop <prop file>] (-listAll|-listIngested|-bag <bagname>)
    AptQuery -h
      usage: AptQuery
      -apiprop <arg>   API Config File (default: api.prop)
      -bag <arg>       Returns the Ingest Stats for a Bag Name.  ETAG will be
                       returned if successful.
      -debug           Output debug messages
      -h               Help Info
      -listAll         Returns a tab-separated list of All Inventory Items
                       (including failures)
      -listIngested    Returns a tab-separated list of All Successfully
                       Ingested Items

# Note
_Georgetown University Library IT has developed the following application for packaging APTrust bag files: https://github.com/Georgetown-University-Libraries/File-Analyzer/wiki/Bagit-automation-for-Academic-Preservation-Trust-(APTrust)_

***
[![Georgetown University Library IT Code Repositories](https://raw.githubusercontent.com/Georgetown-University-Libraries/georgetown-university-libraries.github.io/master/LIT-logo-small.png)Georgetown University Library IT Code Repositories](http://georgetown-university-libraries.github.io/)

