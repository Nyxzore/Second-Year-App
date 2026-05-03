# Gon (Goal Tracker App)

**Gon** is a mobile application developed as a joint project for **COMS2013A & COMS2002A** (Second Year Computer Science). The application focuses on personal goal management and utilizes remote database mechanics to synchronize data.

## Members
* Damian Nel
* Gabriela Fernandes
* Nickson He
* Raymond Gordon

## Theme
Personal productivity and goal tracking. The app provides a centralized platform for users to define, track, and manage their objectives with friends!

## Features
* **Secure Authentication**: User login system featuring SHA-256 password hashing for secure data transmission.
* **Goal Dashboard**: A clean interface to view all active goals, including titles, descriptions, and target dates.
* **Goal Creation, Editing, Deleting**
* **Cloud Sync**: Real-time interaction with a remote PHP backend via `OkHttp` to ensure data is stored safely off-device.

## Tech Stack
* **Frontend**: Java (Android SDK)
* **UI/UX**: Material Components (RecyclerView)
* **Networking**: OkHttp 3
* **Backend**: PHP / PostreSQL
* **Security**: SHA-256 Hashing, UUID

## Approach
The project follows a client-server architecture. The Android client handles the user interface and local logic, while a remote PHP server manages the database interactions (CRUD operations for goals and user validation). This ensures that user data is accessible across different devices upon login.
