# Trello-clone v2

## Project Overview
- this will be the second version of the Trello clone: a Kanban board application, using spring boot as backend
and react as frontend
- user will be allowed to register and login with email and password, including a email verification step, or using
OAuth2 with Google, Facebook, or Github
- application will use Spring Security for authentication and authorization. After login, an access token and 
a refresh token will be generated and stored in cookies.
- this application will use the PostgreSQL as the primary database, using in-memory Caffeine cache for current
development stage. 
- users will be able to create boards, lists, and cards. Each board can have multiple lists, and each list can have
multiple cards. Users can move cards between lists, assign users to cards, attach labels, adding comments and
attachments to cards.
- this application will have a real-time collaboration feature using WebSocket, so that users can see changes,
and may using NoSql like MongoDB for storing real-time data, and a Chat channel of the board will be built in future

## User
* user is the main entity of the application, and will have the following properties:
  * UID: a unique identifier for the user
  * Email: the primary email address of the user
  * Password: the password of the user (hashed), can be null if the user is registered with OAuth2
  * Name: the name of the user
  * Avatar: a URL to the user's avatar image
  * is Verified: a boolean value indicating whether the user's email is verified or not
  * role: the role of the user in the application (Admin, User)
    * Admin: can manage users and boards
    * User: can create and manage their own boards
* including metadata:
  * Created At: the date and time when the user was created
  * Updated At: the date and time when the user was last updated
  * status: the status of the user (Active, Locked, Archived)
    * Active: the user can login and use the application
    * Locked: the user cannot login, but can be reactivated
    * Archived: the user is archived, and cannot login or use the application

## Board
* Board also contains:
  * Title: the name of the board
  * Description (optional): a short description of the board
  * closed: a boolean value indicating whether the board is closed or not
    * closed board can only be viewed, but not edited, but admin can reopen it
  * Members: users who are members of the board
  * Labels: labels that can be used in the board
  * Lists: lists that are part of the board
* including metadata:
  * Created At: the date and time when the board was created
  * Updated At: the date and time when the board was last updated
  * Created By: the user who created the board
  * is Archived: a boolean value indicating whether the board is archived or not

### Board Members
* Board Members are users who are part of the board:
  * User: the user who is a member of the board
  * Board: the board that the user is a member of
  * Role: the role of the user in the board (Admin, Member, Leave)
    * Member: can view and edit the board
    * Admin: can view, edit, and manage the board (add/remove members, change settings)
    * Viewer: can view the PRIVATE board, but cannot edit
* members can also have customization on board:
  * is Starred: a boolean value indicating whether the board is starred or not
* including metadata:
  * Created At: the date and time when the member was added to the board
  * Updated At: the date and time when the member was last updated
  * active: a boolean value indicating whether the member is active or not
    * Active: the member can view and edit the board
    * Inactive: the member has left the board, just as a regular user

## List
* list is a collection of cards:
  * Title: the name of the list
  * Color (optional): the color of the list
  * Position: the position of the list in the board
  * Board: the board that the list belongs to
  * Cards: cards that are part of the list
* metadata:
  * Created At: the date and time when the list was created
  * Updated At: the date and time when the list was last updated
  * Created By: the user who created the list
  * is Archived: a boolean value indicating whether the list is archived or not

## Card
* card is a task or an item in the list:
  * Title: the name of the card
  * Description (optional): a short description of the card
  * Position: the position of the card in the list
  * List: the list that the card belongs to
  * Labels: labels that are attached to the card
  * Comments: comments that are attached to the card
  * Attachments: files that are attached to the card
  * Due Date (optional): a date and time when the card is due
  * Assigned Users: users who are assigned to the card

### Read Board content

### More
- implement in future