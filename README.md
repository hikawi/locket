# Locket

## Description

A clone of the Locket application, with the following tech stack:

- **Frontend**: Android only (Java) + XML
- **Backend**: Spring Boot (Java)
- **Image Cloud**: Cloudinary
- **Database**: PostgreSQL v17.2

## Database System

Runs on PostgreSQL v17.2, currently self-hosted on a VPS (can be changed to Supabase once everyone is on board). URL `postgresql://locket.frilly.dev:5432`, but it's currently securely locked, and only connections from the **same** device would be able to connect.

Note: all `text` datatypes mean `varchar(255)`. `fulltext` means long text.

**Users**:

|   Column   | DataType | Constraint           |
| :--------: | :------: | -------------------- |
|     id     |  bigint  | primary key identity |
|  username  |   text   | unique not null      |
|   email    |   text   | unique not null      |
|  password  |   text   | unique not null      |
| birthdate  |   date   | not null default now |
| avatar_url |   text   |                      |
|    role    |   text   | not null             |

**Friendships**:

| Column | DataType | Constraint                       |
| :----: | :------: | -------------------------------- |
| user1  |  bigint  | primary key references users(id) |
| user2  |  bigint  | primary key references users(id) |

**Friend Requests**:

|   Column    | DataType  | Constraint                    |
| :---------: | :-------: | ----------------------------- |
|     id      |  bigint   | primary key identity          |
|  sender_id  |  bigint   | not null references users(id) |
| receiver_id |  bigint   | not null references users(id) |
|    time     | timestamp | not null default now          |

**Posts**:

|   Column   | DataType  | Constraint                    |
| :--------: | :-------: | ----------------------------- |
|     id     |  bigint   | primary key identity          |
|  user_id   |  bigint   | not null references users(id) |
| image_link |   text    | not null                      |
|  message   | fulltext  |                               |
|    time    | timestamp | not null default now          |

**Post Viewers**:

| Column  | DataType | Constraint                       |
| :-----: | :------: | -------------------------------- |
| post_id |  bigint  | primary key references posts(id) |
| user_id |  bigint  | primary key references users(id) |

**Comments**:

| Column  | DataType  | Constraint                    |
| :-----: | :-------: | ----------------------------- |
|   id    |  bigint   | primary key identity          |
| user_id |  bigint   | not null references users(id) |
| post_id |  bigint   | not null references posts(id) |
| content | fulltext  | not null                      |
|  time   | timestamp | not null default now          |

**Reactions**:

|  Column  | DataType | Constraint                       |
| :------: | :------: | -------------------------------- |
| post_id  |  bigint  | primary key references posts(id) |
| user_id  |  bigint  | primary key references users(id) |
| reaction |   text   | not null enum                    |

**Messages**:

|   Column    | DataType  | Constraint                    |
| :---------: | :-------: | ----------------------------- |
|     id      |  bigint   | primary key identity          |
|  sender_id  |  bigint   | not null references users(id) |
| receiver_id |  bigint   | not null references users(id) |
|   content   | fulltext  | not null                      |
|    state    |   text    | not null                      |
|    time     | timestamp | not null default now          |

## Backend System

Currently hosted on `https://locket.frilly.dev/`. This does not sync with Git, deploying requires rsync-ing over and re-running the .jar file. The backend is currently on **Spring Boot 3**, ran on **Java 21**.

### Routes

Security is done via a header `Authorization` with `Bearer <token>`. There are routes that are unauthenticated:

- POST `/login`
- POST `/register`
- GET `/`
- GET `/profiles` (partially)

Trying to access a route that does not exist always returns `404 NOT FOUND`. Trying to access an authenticated route without a valid token always returns `401 UNAUTHORIZED`.

Most routes accept `application/json`. Except a few routes that need image data, therefore, those would accept `multipart/form-data` instead.

#### GET `/`

- Gets the server's active version. This is for checking if the server's up to date, for example, if you changed something and the Git's version is 1.1, but the server still says 1.0, the server hasn't been updated.
- Returns:
  - 200 with { version: string }

> You can check the Git's version in LocketBackendApplication.kt file.

#### POST `/login`

- Logins with an existing account.
- Accepts body:
  - username (string)
  - password (string)
- Returns:
  - 404 if username not found
  - 403 if username found, password incorrect
  - 200 with { token: string } returned

#### POST `/register`

- Registers a new account.
- Accepts body:
  - username (string)
  - email (string)
  - password (string)
- Returns:
  - 400 if some fields are missing, or email is not an email, or username is not in correct format
  - 409 if username or email is taken
  - 200 if success, returns { token: string }

#### GET `/profiles`

- Retrieves user's information. If user is authenticated AND username is not provided, then it fetches the user's own profile. If username is provided, it will always fetch that user's profile.
- Accepts query:
  - username (string, optional)
- Returns:
  - 404 if username is not found OR when not authenticated and didn't provide a username
  - 200 along with { username: string, email: string, birthdate: date?, avatar: string? }

#### PUT `/profiles`

- Updates profile's information. Provide the field to change, if no change, don't put in the body.
- Accepts query:
  - username (string?)
  - email (string?)
  - password (string?)
  - birthdate (date?)
- Returns:
  - 409 if username or email is already used by another user (different user id)
  - 200 with { username: string, email: string, birthdate: date?, avatar: string? }

#### GET `/requests`

- Retrieves a list of friend requests.
- Returns:
  - 200 with { results: { username: string, avatar: string? }[] }

#### POST `/requests`

- Sends a friend request or accepts one.
- Accepts body:
  - username: string
- Returns:
  - 409 if you already sent a request to that person
  - 403 if the target user is yourself
  - 404 if the target user can not be found
  - 204 if the target has sent you a request, you just accepted it
  - 200 if the request was sent successfully, returns { username: string }

#### DELETE `/requests`

- Denies a friend request.
- Accepts body:
  - username: string
- Returns:
  - 404 if the target can not be found
  - 204 if nothing was deleted
  - 200 if the request was deleted

#### GET `/friends`

- Retrieves a list of my friends.
- Accepts query:
  - page (int, default 0)
  - per_page (int, default 20)
- Returns:
  - 200 with { total: long, totalPages: int, results: { username: string, avatar: string? }[] }

#### DELETE `/friends`

- Removes a friend.
- Accepts body:
  - username (string)
- Returns:
  - 404 if username not found
  - 204 if you weren't friends in the first place
  - 200 if friendship deleted

#### GET `/posts`

- Retrieves a list of posts I can see, ranging in a timeframe.
- Accepts query:
  - from (date)
  - to (date, default now)
- Returns:
  - 200 with { total: long, results: { id: long, poster: { username: string, avatar: string? }, image: string, message: string?, time: date } }

#### POST `/posts`

- Post an image.
- Accepts body (multipart/form-data):
  - image (file): The image file itself, accepts 10MB max, allows PNG, JPG, WEBP.
  - message (string?): Optional, the message of the post
  - viewers (string): A comma-separated list of users to share with. Eg: vohoangduc,ducthien,thehung. Empty means private.
- Returns:
  - 400 if the file is not a valid image
  - 413 if the file is too big
  - 201 with header "Location" pointing to the image link

#### PUT `/posts`

- Edit a post. If a field is provided, that field will be changed.
- Accepts body (multipart/form-data):
  - id (long): the post ID
  - image (file?): optional image file, same as POST `/posts`
  - message (string?): optional
  - viewers (string?): optional
- Returns:
  - 404 if the post ID doesn't exist
  - 403 if the post wasn't yours
  - 400 if the file is invalid image
  - 413 if the file is too big
  - 200 with { id: long, poster: { username: string, avatar: string? }, image: string, message: string?, time: date }

#### DELETE `/posts`

- Delete a post.
- Accepts body:
  - id (long): the post ID to delete
- Returns:
  - 404 if the post ID was not found
  - 403 if the post's author wasn't you
  - 200 with { id: long, poster: { username: string, avatar: string? }, image: string, message: string?, time: date }

#### GET `/messages` (v0.4.1)

- Get messages for me within a certain time frame.
- Accepts query:
  - page (int, default 0)
  - per_page (int, default 20)
  - since (local date time)
  - until (local date time, optional)
- Always returns 200 with a paginated object { total, totalPages, page, perPage, results: { id, sender, receiver, content, time, state }[] }

#### POST `/messages` (v0.4.1)

- Send a new message to another user.
- Accepts body:
  - receiver (long): The user's ID
  - content (string): The content string
- Returns:
  - 400 if receiver is negative, or content is empty
  - 404 if the receiver ID can not be found
  - 403 if the receiver is not your friend
  - 200 with { id, sender, receiver, content, time, state }

#### DELETE `/messages` (v0.4.1)

- Delete a sent message.
- Accepts body:
  - id (long): the message ID
- Returns:
  - 403 if the message is not yours
  - 204 if the message doesn't exist
  - 200 if success, returns { id, sender, receiver, content, time, state }

## Image System

The image system has not been properly configured. There are currently 2 routes:

- Hosting on **Cloudinary**.
- Hosting on a self-hosted **Amazon S3 Instance**.

I setup with **Cloudinary**, but we can change to Amazon's S3 if we want.

## Frontend System

- Android API v28
- Device: Pixel Pro (API v35)
- Language: Java 21.0.5 Temurin
