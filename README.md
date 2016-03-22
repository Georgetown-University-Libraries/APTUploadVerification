# APTUploadVerification
Command Line Interface to the APTrust API: https://sites.google.com/a/aptrust.org/member-wiki/member-api/api-basics

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
      -bag <arg>       Show Status for Bag Name
      -debug           Output debug messages
      -h               Help Info
      -listAll         List All Inventory Items (including failures)
      -listIngested    List All Successfully Ingested Items

# License information is contained below.

    ------------------------------------------------------------------------
    
    Copyright (c) 2013, Georgetown University Libraries
    All rights reserved.
    
    Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
    
    Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
    Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
    THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
