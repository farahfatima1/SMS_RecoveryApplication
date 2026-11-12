# **SMS Recovery Application**

# 

**An Android-based SMS Data Recovery and Archiving Application**

SMS Recovery Application is an Android project developed using Kotlin and Android development technologies. It focuses on SMS data recovery, local SMS archiving, database parsing, message export, and integrity verification.

The project aims to provide an organized approach to recovering, managing, archiving, and preserving SMS data on Android devices.



###### **Project Overview**



SMS Recovery Application brings together different SMS-related functionalities in one Android application. It includes components for SMS database parsing, recovery processing, automatic archiving, exporting recovered messages, and generating integrity hashes.



The project also explores digital-forensics-oriented practices for maintaining the integrity of recovered SMS evidence.

###### 

###### **Key Features**



\- \*\*SMS Recovery:\*\* Components for processing and recovering available SMS records.

\- \*\*Deleted SMS Recovery:\*\* A dedicated recovery engine for examining potentially recoverable deleted SMS data.

\- \*\*SMS Database Parsing:\*\* Parses SMS database records for recovery and examination.

\- \*\*Automatic SMS Archiving:\*\* Includes SMS archive storage, receiver, and observer components.

\- \*\*SMS Export:\*\* Supports exporting SMS data through the application's export functionality.

\- \*\*Integrity Verification:\*\* Uses hashing functionality to support SMS data integrity verification.

\- \*\*Android Application Interface:\*\* Provides an Android application interface for accessing SMS recovery and related functionality.



\*Note: Actual recovery results depend on device access, Android restrictions, available data, and the condition of the source database. Permanently deleted messages cannot be guaranteed to be recovered.\*

###### 

###### **Technology Stack**



| Technology | Purpose |

|---|---|

| Kotlin | Application development |

| Android Studio | Development environment |

| Jetpack Compose | Android UI development |

| Gradle | Build automation |

| SQLite | SMS database-related operations |

| SHA-256 | Data integrity hashing |

| Android SDK | Android platform integration |



###### **Project Structure**



```text

SMS\_RecoveryApplication/

│

├── app/

│   ├── src/

│   │   ├── main/

│   │   │   ├── java/com/example/data\_recoveryapplication/

│   │   │   │   ├── MainActivity.kt

│   │   │   │   ├── RecoveredSmsStore.kt

│   │   │   │   ├── SmsArchiveDatabase.kt

│   │   │   │   ├── SmsArchiveReceiver.kt

│   │   │   │   ├── SmsAutoArchiveObserver.kt

│   │   │   │   ├── SmsDatabaseParser.kt

│   │   │   │   ├── SmsDeletedRecoveryEngine.kt

│   │   │   │   ├── SmsExportManager.kt

│   │   │   │   ├── SmsExportScreen.kt

│   │   │   │   ├── SmsIntegrityHasher.kt

│   │   │   │   ├── SmsRecoveryEngine.kt

│   │   │   │   ├── SmsRecoverySource.kt

│   │   │   │   └── ui/theme/

│   │   │   │

│   │   │   ├── AndroidManifest.xml

│   │   │   └── res/

│   │   │

│   │   ├── androidTest/

│   │   └── test/

│   │

│   └── build.gradle.kts

│

├── gradle/

│   └── wrapper/

│

├── build.gradle.kts

├── gradle.properties

├── gradlew

├── gradlew.bat

├── settings.gradle.kts

└── .gitignore

```



Getting Started



###### &#x20;**Prerequisites**



Before running the project, make sure you have:



\- Android Studio installed.

\- Android SDK configured.

\- Git installed.

\- A compatible JDK configured for the project's Gradle version.

\- An Android device or emulator.

###### 

###### &#x20;**Installation**



1\. Clone the repository



```bash

git clone https://github.com/farahfatima1/SMS\_RecoveryApplication.git

```



2\. Open the project



Open Android Studio, select \*\*Open\*\*, and choose the cloned `SMS\_RecoveryApplication` folder.



3\. Sync Gradle



Allow Android Studio to synchronize the Gradle dependencies.



4\. Run the application



Connect an Android device or start an emulator, then click the Run button in Android Studio.

###### 

###### **Build Using PowerShell**



You can also build the debug APK using:



```powershell

.\\gradlew.bat assembleDebug

```



The generated APK is normally available at:



```text

app/build/outputs/apk/debug/app-debug.apk

```



###### **Limitations**



The current implementation has the following limitations:



\- \*\*Emulator-Only Support:\*\* The application is currently designed and tested for an Android emulator/simulator environment. Physical Android device support has not yet been established.

\- \*\*Application Installation Requirement:\*\* SMS data recovery and archiving depend on the application's installation and its available local archive or stored data. The application cannot independently retrieve previously deleted SMS messages from devices where it was never installed.

\- \*\*Local Data Dependency:\*\* Recovery functionality depends on the SMS records and archived data accessible to the application.

\- \*\*Android Restrictions:\*\* SMS access and recovery operations are subject to Android permissions, storage restrictions, and operating-system security mechanisms.

\- \*\*No Guaranteed Recovery:\*\* Permanently deleted, overwritten, or unavailable SMS records cannot be guaranteed to be recovered.



###### &#x20;**Future Improvements**



\- Extend compatibility from Android emulators to physical Android devices.

\- Investigate additional authorized SMS database examination methods.

\- Improve recovery of available SMS records and archived data.

\- Enhance forensic evidence integrity and reporting.

\- Perform compatibility and functionality testing across different Android versions.

###### &#x20;**Project Information**



| Detail | Information |

|---|---|

| Project Name | SMS Recovery Application |

| Platform | Android |

| Programming Language | Kotlin |

| Development Environment | Android Studio |

| Repository | farahfatima1/SMS\_RecoveryApplication |

| Project Category | Android Development / Digital Forensics |



###### &#x20;**Author**



Farah Fatima



Cybersecurity and Digital Forensics Student



GitHub: \[@farahfatima1](https://github.com/farahfatima1)

###### 

###### **Disclaimer**



This project is intended for educational, research, and authorized data recovery purposes. Users are responsible for ensuring that they have appropriate authorization before accessing or recovering SMS data.



