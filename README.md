# PaP

Panim El-Panim (Face to Face) is a movement that attempts to close the gaps between the different parts of the nation.
Activist go house to house and just talk about the important topics to create an understanding between people that normally have no contact.

The code works with global vars on the idea that at no point can towo houses be active.

The app has a lot of missing features but it is a free app and I do not have the time to work on it:
1. No google search bar to find an address (use some free geocoding third party API)
2. There needs to be a way to stop the user from scrolling or using the my loacation butoon or the searchbar to get out of the relevant zone.
3. Make an onclick on the latestReport open a list of all the reports of the house.
4. Add an option to update the picture.
5. Block users from submiting an item without the basic information.
6. He (Shmuel) wants to be able to add zones - I thought youcan do it as markers and make the main menu activity a google maps activity as well (all the cameraFactoryUpdate info when you open a zone can be in the server).
7. Clean up the code - a lot of things can be more efficient and primarily - when I built it for some stupid reason I did two almost identical functions for displaying data - one for when you just built the house (and you have a House var) and one for when you dowload the data from the server(and you have a HashMap var). Build a constructor that turns the HashMap to House and delete the irrelevant function.

Known Bugs:
1. The keyboard won't go down for some users and that hides the submit button which is why I put it near the top. Fix it and put him back down.

For any questions my personal email is nachumolman@gmail.com. Send mails with the subject "About the App...".
