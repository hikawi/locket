# Locket

## Description

A clone of the Locket application, with the following tech stack:

- Frontend: Android only (Java) + XML
- Backend: Spring Boot (Kotlin) (_can be changed to ExpressTS if necessary_)
- Database: PostgreSQL v17.2

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

| Column | DataType | Constraint  |
| :----: | :------: | ----------- |
| user1  |  bigint  | primary key |
| user2  |  bigint  | primary key |

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

Currently hosted on `https://locket.frilly.dev/`. This does not sync with Git, deploying requires rsync-ing over and re-running the .jar file.

Choose one system, currently on **Spring Boot**.

|    Stack    |      Spring Boot      |             Express             |
| :---------: | :-------------------: | :-----------------------------: |
|  Language   |     Kotlin, Java      |           TypeScript            |
|  Security   | Spring Security + JWT |               JWT               |
|   Driver    |      Spring JDBC      |           DrizzleKit            |
|     ORM     |   Spring Hibernate    | DrizzleORM / Sequelize / KnexJS |
|    HTTP     |      Spring Web       |         _already HTTP_          |
| Validation  |   Spring Validation   |               Zod               |
| How to run  |  `./gradlew bootRun`  |       `npx tsx index.ts`        |
| Environment |      Java 21.0.5      |            Node v22             |

How it works? (I think):

- **Express**: HTTP request -> Router -> Middlewares -> Controller
- **Spring**: HTTP request -> Spring Security (Authentication Entrypoint -> Authentication Filter(s)) -> Controller (and Controller Advice)

## Image System

The image system has not been properly configured. There are currently 2 routes:

- Hosting on **Cloudinary**.
- Hosting on a self-hosted **Amazon S3 Instance**.

I setup with **Cloudinary**, but we can change to Amazon's S3 if we want.

## Frontend System

- Android API v24
- Device: Pixel Pro (API v35)
- Language: Java
