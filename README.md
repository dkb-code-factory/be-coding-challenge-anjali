# Read Me First

All the information about the code challenge is in [CODE_CHALLENGE.md](./CODE_CHALLENGE.md)

## Design Decisions: Long-Term Solution

The challenge required a long-term solution to handle growing notification types within stable categories.

---

## Core Solution

The root cause of the problem was coupling the user's subscription directly to a static list of notification types (`user_notifications` table).

The solution implemented **decouples** the user subscription from individual notification types and couples it to the higher-level **Category** instead.

1. **New Subscription Model**  
   The database schema and service logic were updated to store the user's subscription against the Category (e.g., `User(userId, Category='A')`).

2. **Mapping Table**  
   A new, persistent mapping table (`category_notifications`) stores the relationship: **(Category, NotificationType)**.  
   This makes it future proof to add new categories easily.

3. **Migration Script**  
   Migrates `user(userId, notification_types)` → `user(userId, category)`.  
   Assumption: migration occurs during planned downtime to avoid inconsistent data.

---

## Design Decisions & Thought Process

### 2. Avoiding a separate `user_category` table

Initially, I considered creating a separate table called `user_category` to store user–category mappings in addition to the user table.  
Each row would essentially contain just a user_id and a category.  
However, I decided not to go with this approach because:

- Creating a separate table for just a single extra column adds unnecessary complexity.
- It would require additional joins for most queries without providing significant benefits.
- It would also make the logic for adding new notification types cumbersome, since existing users would need manual updates to maintain the new types.

---

### 3. Not storing notification types directly in users

Storing the notification types directly in `user_notifications` was rejected because:

1. It would tightly couple the user subscription to individual types.  
   With millions of users, iterating over each one every time a new notification is added can create inconsistent data and race conditions.

2. Every new notification type would require updating all existing user subscriptions to include it.

3. The table would grow unnecessarily large and introduce redundant data since multiple users share the same category.

---

### 4. Category-based subscription model (Chosen approach)

Instead, I chose to store only the **categories** that a user is subscribed to, and maintain a mapping table for **category → notification type** (`category_notifications`).  
This has several advantages:

- When a new type is added to a category, all existing users subscribed to that category automatically receive it — **no subscription updates needed**.
- Sending notifications only requires looking up the category of the type and checking if the user is subscribed to that category.  
  Queries become simpler.
- Adding a new notification type to a category is a **single administrative transaction**, which automatically benefits all relevant users.

---

### 5. Backward compatibility

- Endpoints remain consistent (`/register`, `/notify`), so client-side behavior is unchanged.
- Existing users automatically benefit from new types in their subscribed categories without re-registration.
- Reduced risk of inconsistencies and manual errors, since the mapping is maintained centrally.



## Endpoint Changes Summary

| Endpoint | Logic Implemented | Benefit |
|---------|-------------------|---------|
| **POST /register** | Category-Based Registration: Reads the input notification types, looks up all corresponding categories via the mapping table, and subscribes the user to the Categories derived from those types. | Future-proof: When new types are added to a category, existing category subscriptions cover them automatically. |
| **POST /notify** | Dynamic Category Check: Looks up the notificationType in the mapping table to find its associated Category. It then checks if the userId is subscribed to that specific Category. | Solves the problem: Users get new types (type6) because they are checked against Category A, not a static list of types. |
| **POST /admin/notification-type** | Mapping Management: An administrative endpoint was added to allow Marketing/DevOps to add new (Category, NotificationType) mappings. Includes validation to prevent a type from being mapped to two different categories. | Minimal effort: Adding a new type is now a single administrative transaction. |

# Getting Started

1. start docker-compose with postgres
2. start the app
3. Hit the following endpoints to test the service:

```bash
curl -X POST -H "Content-Type: application/json" localhost:8080/register -d '{ "id": "bcce103d-fc52-4a88-90d3-9578e9721b36", "notifications": ["type1","type5"]}'
curl -X POST -H "Content-Type: application/json" localhost:8080/notify -d '{ "userId": "bcce103d-fc52-4a88-90d3-9578e9721b36", "notificationType": "type5", "message": "your app rocks!"}'
```


