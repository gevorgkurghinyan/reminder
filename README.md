# Pre-work - Reminder

Reminder is an android app that allows building a reminder/todo list and basic todo items management functionality including adding new items, editing and deleting an existing item.

Submitted by: Gevorg Kurghinyan

Time spent: 14 hours spent in total

## User Stories

The following **required** functionality is completed:

* [yes] User can **successfully add and remove items** from the todo list
* [yes] User can **tap a todo item in the list and bring up an edit screen for the todo item** and then have any changes to the text reflected in the todo list.
* [yes] User can **persist todo items** and retrieve them properly on app restart

The following **optional** features are implemented:

* [yes] Persist the todo items [into SQLite](http://guides.codepath.com/android/Persisting-Data-to-the-Device#sqlite) instead of a text file
* [yes] Improve style of the todo items in the list [using a custom adapter](http://guides.codepath.com/android/Using-an-ArrayAdapter-with-ListView)
* [yes] Add support for completion due dates for todo items (and display within listview item)
* [No] Use a [DialogFragment](http://guides.codepath.com/android/Using-DialogFragment) instead of new Activity for editing items
* [yes] Add support for selecting the priority of each todo item (and display in listview item)
* [yes] Tweak the style improving the UI / UX, play with colors, images or backgrounds


## Video Walkthrough

Here's a walkthrough of implemented user stories:

<img src='http://i.imgur.com/SPFxZeq.gif' title='Video Walkthrough' width='' alt='Video Walkthrough' />

GIF created with [LiceCap](http://www.cockos.com/licecap/).

## Project Analysis

As part of your pre-work submission, please reflect on the app and answer the following questions below:

**Question 1:** "What are your reactions to the Android app development platform so far? Compare and contrast Android's approach to layouts and user interfaces in past platforms you've used."

**Answer:** First of all Android Studio is a great IDE and very easy to use. Android SDK provides a lot of layout components and views
            which makes UI designing process very straightforward, especially when you follow Material design guidlines. There is a very big
            community of Android engineers and a really good document and video materials for every topic, so one can easally find answers
            to his issues.

**Question 2:** "Take a moment to reflect on the `ArrayAdapter` used in your pre-work. How would you describe an adapter in this context and what is its function in Android? Why do you think the adapter is important? Explain the purpose of the `convertView` in the `getView` method of the `ArrayAdapter`."

**Answer:** I have used custom CursorAdampter instead of ArrayAdapter to read reminder items from the persistent storage (in my case from Sqlite DB) and to display items on ListView.

## Notes

Describe any challenges encountered while building the app.

## License

    Copyright [yyyy] [name of copyright owner]

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
