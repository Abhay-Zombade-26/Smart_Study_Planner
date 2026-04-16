# 📚 Smart Study Planner

<div align="center">

![Java](https://img.shields.io/badge/Java-17-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Swing](https://img.shields.io/badge/UI-Java_Swing-4A90D9?style=for-the-badge&logo=java&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-8.0-005C84?style=for-the-badge&logo=mysql&logoColor=white)
![Maven](https://img.shields.io/badge/Build-Maven-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white)
![Groq AI](https://img.shields.io/badge/AI-LLaMA_3.3_70B-blueviolet?style=for-the-badge)
![OAuth](https://img.shields.io/badge/OAuth2-Google_%7C_GitHub-4285F4?style=for-the-badge)
![Version](https://img.shields.io/badge/Version-1.0.0-brightgreen?style=for-the-badge)

**A dual-role desktop productivity application built with Java Swing that helps students plan, track, and complete their learning goals — powered by AI and real GitHub integration.**

[Features](#-features) • [Tech Stack](#-technology-stack) • [Project Structure](#-project-structure) • [Database Schema](#-database-schema) • [Setup](#-getting-started) • [Configuration](#-configuration)

</div>

---

## 🌟 Overview

**Smart Study Planner** is a fully offline-capable Java Swing desktop application designed for two types of learners:

- 🎓 **Normal Students** — who study subjects like Physics, Chemistry, History, and need structured topic-based daily study plans
- 💻 **IT Students / Developers** — who track GitHub repositories, project contributions, and commit-driven goals

Both roles share the same application but operate with **complete data isolation**. The same email address can hold accounts for both roles simultaneously, keeping all plans, tasks, and progress fully separate.

---

## ✨ Features

### 🔐 Authentication & Role System

- **Google OAuth 2.0** — Sign in as a Normal Student (subject-based learning)
- **GitHub OAuth 2.0** — Sign in as an IT Student (repository-based tracking)
- Role-based data separation: `NORMAL` vs `IT`
- Same email address can be registered under both roles with zero data overlap
- Role selection screen on first launch or when multiple roles are detected

---

### 🎓 Normal Student Features (Google Login)

- **Subject-Based Plans** — Create plans for built-in subjects (Marathi, Hindi, English, Physics, Chemistry, Biology, History, Geography) or add any custom subject
- **Topic Management** — Add topics with difficulty and size ratings; weight is auto-calculated as `difficulty × size`
- **Smart Task Generation** — Tasks are distributed evenly across all available days (including the deadline day) using three session types:
  - 30% Learn sessions
  - 40% Practice sessions
  - 30% Review sessions
- **Dashboard** — See today's tasks and overall plan progress at a glance
- **Double-Click to Complete** — Mark tasks done directly from the task list
- **Missed Task Rescheduler** — Automatically redistribute missed tasks to upcoming available days
- **Profile Page** — View active plans, total tasks completed, and personal statistics
- **Modern UI** — Gradient backgrounds, consistent color scheme, and intuitive layouts throughout

---

### 💻 IT Student Features (GitHub Login)

- **Repository Integration** — Select from your own GitHub repositories to create project-based goals
- **AI-Powered Structure** — Auto-generate project structure and tasks using Groq AI based on repo content and project type
- **Multi-Repo Plans** — Create plans spanning multiple repositories with per-repo priority settings (`HIGH`, `MEDIUM`, `LOW`)
- **Live Commit Tracking** — Sync with the GitHub API to track actual commits per task
- **Auto Task Status Updates** — Tasks automatically marked `COMPLETED`, `MISSED`, or `PENDING` based on real commit data
- **GitHub Avatar** — Profile page displays your GitHub profile picture
- **Profile Page** — View active plans and total commits across all repositories
- **Interactive Analytics Dashboard** powered by JFreeChart:
  - 🥧 Pie Chart — Task status distribution (Completed / Missed / Pending)
  - 📈 Line Chart — Daily progress trend over the last 7 days
  - 📊 Bar Chart — Weekly completion summary
- **CSV Export** — Export task data with a file chooser dialog; opens directly in Excel
- **Hover Tooltips** — Truncated task descriptions and repository names reveal full text on hover

---

### 🔄 Common Features

- Create, update, and delete study plans
- Generate tasks from topics or repositories
- Track daily and overall goal progress
- Streak system for motivation and consistency
- Adjust deadlines and daily study hours per plan
- Clean, responsive Swing UI with a unified design language

---

## 🛠️ Technology Stack

| Layer | Technology |
|---|---|
| Desktop UI | Java Swing |
| Charts | JFreeChart |
| Language | Java 17 |
| Database | MySQL 8.0 |
| Build Tool | Apache Maven |
| Auth — Normal Students | Google OAuth 2.0 |
| Auth — IT Students | GitHub OAuth 2.0 |
| AI Features | Groq API — LLaMA 3.3 70B Versatile |
| GitHub Data | GitHub REST API v3 |
| DB Driver | MySQL Connector/J |
| HTTP | Java HttpURLConnection via HttpUtil |
| JSON | Lightweight custom JsonUtil |

---

## 🗂️ Project Structure

```
Smart-Study-Planner/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   ├── app/
│   │   │   │   └── Main.java                       # Application entry point
│   │   │   ├── config/
│   │   │   │   └── AppConfig.java                  # Config file loader
│   │   │   ├── dao/
│   │   │   │   ├── UserDAO.java                    # User CRUD operations
│   │   │   │   ├── StudyPlanDAO.java                # Plans with role-based queries
│   │   │   │   ├── StudyTaskDAO.java                # Tasks (dual save methods)
│   │   │   │   ├── TopicDAO.java                   # Topic management
│   │   │   │   └── GoalDAO.java                    # Goal operations (IT/GitHub)
│   │   │   ├── db/
│   │   │   │   └── DBConnection.java               # Singleton DB connection
│   │   │   ├── enums/
│   │   │   │   └── UserRole.java                   # NORMAL, IT enums
│   │   │   ├── model/
│   │   │   │   ├── User.java                       # User entity with role
│   │   │   │   ├── StudyPlan.java                  # Plan entity (role + login_type)
│   │   │   │   ├── StudyTask.java                  # Task entity
│   │   │   │   ├── Topic.java                      # Topic (Normal students only)
│   │   │   │   ├── DailyTask.java                  # Daily task (GitHub/IT only)
│   │   │   │   └── FeatureTemplate.java            # AI feature templates
│   │   │   ├── service/
│   │   │   │   ├── GoogleAuthService.java          # Google OAuth flow
│   │   │   │   ├── GitHubAuthService.java          # GitHub OAuth flow
│   │   │   │   ├── NormalPlanGenerator.java        # Task generation (Normal)
│   │   │   │   ├── StudyPlanGenerator.java         # Plan generation (GitHub)
│   │   │   │   ├── AIPlanService.java              # Groq AI plan generation
│   │   │   │   ├── PlanRescheduler.java            # Missed task rescheduling
│   │   │   │   ├── TopicWeightCalculator.java      # Weight distribution logic
│   │   │   │   ├── GitHubCommitChecker.java        # GitHub commit verification
│   │   │   │   ├── ProjectTypeDetector.java        # Detect project type from repo
│   │   │   │   └── TemplateLibrary.java            # Feature templates library
│   │   │   ├── ui/
│   │   │   │   ├── LoginFrame.java                 # Login screen with OAuth
│   │   │   │   ├── NormalStudyPlannerFrame.java    # Normal student dashboard
│   │   │   │   ├── ITStudyPlannerFrame.java        # IT student dashboard
│   │   │   │   ├── PlanManagementFrame.java        # Plan management dialog
│   │   │   │   ├── DashboardFrame.java             # Progress dashboard
│   │   │   │   ├── GitHubProgressFrame.java        # Charts and analytics
│   │   │   │   ├── StudyPlanListFrame.java         # View all plans
│   │   │   │   ├── AddTopicDialog.java             # Add topic dialog
│   │   │   │   ├── MultiRepoSelectionDialog.java   # Multi-repo selector
│   │   │   │   ├── ProfileFrame.java               # User profile
│   │   │   │   └── RoleSelectionFrame.java         # Role selection after login
│   │   │   └── util/
│   │   │       ├── HttpUtil.java                   # HTTP request utilities
│   │   │       └── JsonUtil.java                   # JSON parsing utilities
│   │   └── resources/
│   │       ├── config.properties.template          # Configuration template
│   │       └── database/
│   │           └── schema.sql                      # Full DB schema
│   └── test/                                       # Unit tests
├── screenshots/                                    # App screenshots
├── pom.xml                                         # Maven dependencies
└── README.md
```

---

## 🗄️ Database Schema

The application uses a MySQL database named `smart_study_planner`.

---

### 👤 `users`

Stores all authenticated users from both Google and GitHub.

| Column | Type | Description |
|---|---|---|
| `id` | INT PK AUTO_INCREMENT | Unique user ID |
| `name` | VARCHAR(255) | Display name |
| `email` | VARCHAR(255) UNIQUE | User email address |
| `role` | ENUM('NORMAL','IT') | User role |
| `oauth_provider` | VARCHAR(50) | `google` or `github` |
| `github_username` | VARCHAR(255) | GitHub handle (nullable) |
| `access_token` | TEXT | OAuth access token |
| `avatar_url` | TEXT | Profile picture URL |
| `created_at` | TIMESTAMP | Account creation timestamp |
| `active_plan_id` | INT | Reference to currently active plan |

---

### 🎯 `goals`

Study goals and project plans for both user roles.

| Column | Type | Description |
|---|---|---|
| `id` | INT PK | Goal ID |
| `user_id` | INT FK → users | Owning user |
| `repository_name` | VARCHAR(255) | GitHub repo name (IT only) |
| `duration_months` | INT | Plan duration in months |
| `daily_hours` | INT | Study hours per day |
| `start_date` / `end_date` | DATE | Goal timeline |
| `target_commits` | INT | Expected GitHub commits |
| `current_commits` | INT | Actual tracked commits |
| `status` | VARCHAR(50) | `ACTIVE`, `COMPLETED`, `PAUSED` |
| `priority` | VARCHAR(20) | `LOW`, `MEDIUM`, `HIGH` |
| `subject_name` | VARCHAR(255) | Subject name (Normal only) |
| `subjects` | TEXT | Comma-separated subject list |
| `experience_level` | VARCHAR(20) | `BEGINNER`, `INTERMEDIATE`, `ADVANCED` |
| `project_type` | VARCHAR(50) | Project type (IT only) |
| `target_features` | TEXT | Developer-specified feature targets |
| `ai_generated` | BOOLEAN | Whether this plan was AI-generated |
| `difficulty` | VARCHAR(50) | Overall difficulty rating |
| `completion_percentage` | INT | Progress from 0 to 100 |
| `plan_name` | VARCHAR(255) | Friendly name for the plan |
| `role` / `user_role` | VARCHAR(20) | Role context at creation time |
| `login_type` | VARCHAR(20) | `GOOGLE` or `GITHUB` |

---

### ✅ `study_tasks`

Individual daily tasks linked to a goal.

| Column | Type | Description |
|---|---|---|
| `id` | INT PK | Task ID |
| `user_id` | INT FK → users | Owning user |
| `goal_id` | INT FK → goals | Parent goal |
| `topic_id` | INT | Linked topic (0 if none) |
| `task_date` | DATE | Scheduled date for the task |
| `planned_hours` | INT | Hours allocated for task |
| `actual_hours` | INT | Hours actually spent |
| `planned_commits` | INT | Expected commit count |
| `actual_commits` | INT | Actual commits made |
| `status` | VARCHAR(50) | `PENDING`, `IN_PROGRESS`, `COMPLETED`, `MISSED` |
| `description` | TEXT | Task content / instructions |
| `session_type` | VARCHAR(20) | `LEARN`, `PRACTICE`, `REVIEW`, `CODING` |
| `required_commit` | BOOLEAN | Whether a commit is mandatory for this task |

---

### 📖 `topics`

Topic breakdown for Normal student plans.

| Column | Type | Description |
|---|---|---|
| `id` | INT PK | Topic ID |
| `plan_id` | INT FK → goals | Parent plan |
| `subject` | VARCHAR(255) | Subject category |
| `name` | VARCHAR(255) | Topic name |
| `difficulty` | INT (1–5) | Difficulty rating |
| `size` | INT | Relative size / effort level |
| `weight` | DOUBLE | `difficulty × size` — drives scheduling distribution |

---

### 🐙 `github_activity`

Per-repository commit tracking for IT students.

| Column | Type | Description |
|---|---|---|
| `id` | INT PK | Record ID |
| `user_id` | INT FK → users | GitHub user |
| `repo_name` | VARCHAR(255) | Repository name |
| `commit_count` | INT | Total commits tracked |
| `last_commit_date` | DATE | Date of most recent commit |
| `streak_count` | INT | Current active commit streak |
| `last_updated` | TIMESTAMP | Auto-updated on sync |

---

### ⚡ Performance Indexes

```sql
CREATE INDEX idx_goals_user_id   ON goals(user_id);
CREATE INDEX idx_tasks_user_id   ON study_tasks(user_id);
CREATE INDEX idx_tasks_goal_id   ON study_tasks(goal_id);
CREATE INDEX idx_topics_plan_id  ON topics(plan_id);
CREATE INDEX idx_github_user_id  ON github_activity(user_id);
```

---

## 🚀 Getting Started

### Prerequisites

| Tool | Minimum Version |
|---|---|
| Java JDK | 17 |
| MySQL | 8.0 |
| Apache Maven | 3.8 |
| Git | Any |

---

### 1. Clone the Repository

```bash
git clone -b abhi7 https://github.com/Abhay-Zombade-26/Smart_Study_Planner.git
cd Smart_Study_Planner
```

---

### 2. Set Up the Database

```bash
mysql -u root -p < src/main/resources/database/schema.sql
```

This creates the `smart_study_planner` database, all five tables, foreign key constraints, and performance indexes from scratch.

---

### 3. Configure the Application

Copy the template:

```bash
cp src/main/resources/config.properties.template src/main/resources/config.properties
```

Then open `config.properties` and fill in every field:

```properties
# ─── GitHub OAuth ───────────────────────────────────────────
github.client.id=YOUR_GITHUB_CLIENT_ID
github.client.secret=YOUR_GITHUB_CLIENT_SECRET
github.redirect.uri=http://localhost:8888/github-callback

# ─── Google OAuth ───────────────────────────────────────────
google.client.id=YOUR_GOOGLE_CLIENT_ID
google.client.secret=YOUR_GOOGLE_CLIENT_SECRET
google.redirect.uri=http://localhost:8888/google-callback

# ─── Database ───────────────────────────────────────────────
db.url=jdbc:mysql://localhost:3306/smart_study_planner?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
db.user=YOUR_DB_USERNAME
db.password=YOUR_DB_PASSWORD

# ─── Groq AI (Free) ─────────────────────────────────────────
ai.api.key=YOUR_GROQ_API_KEY
ai.api.url=https://api.groq.com/openai/v1/chat/completions
ai.model=llama-3.3-70b-versatile
ai.max.tokens=2000

# ─── Application ────────────────────────────────────────────
app.name=Smart Study Planner
app.version=1.0.0
app.debug=true
```

> ⚠️ `config.properties` is listed in `.gitignore`. Never commit it to version control.

---

### 4. Register OAuth Applications

#### GitHub OAuth App
1. Go to [GitHub Developer Settings](https://github.com/settings/developers) and click **New OAuth App**
2. Set **Homepage URL** to `http://localhost:8888`
3. Set **Authorization callback URL** to `http://localhost:8888/github-callback`
4. Copy the **Client ID** and **Client Secret** into `config.properties`

#### Google OAuth App
1. Open [Google Cloud Console](https://console.cloud.google.com/) → **APIs & Services** → **Credentials**
2. Create a new **OAuth 2.0 Client ID** of type **Web Application**
3. Add `http://localhost:8888/google-callback` as an **Authorized Redirect URI**
4. Copy the **Client ID** and **Client Secret** into `config.properties`

#### Groq API Key (Free)
1. Sign up at [console.groq.com](https://console.groq.com)
2. Go to **API Keys** and generate a new key
3. Paste it as the value of `ai.api.key` in `config.properties`

---

### 5. Build and Run

```bash
# Build the project
mvn clean install

# Run via Maven
mvn exec:java -Dexec.mainClass="app.Main"

# Or run the packaged JAR directly
java -jar target/smart-study-planner-1.0.0.jar
```

The Java Swing desktop window will launch automatically.

---

## ⚙️ Configuration Reference

| Property | Description | Example Value |
|---|---|---|
| `github.client.id` | GitHub OAuth App Client ID | `Ov23li...` |
| `github.client.secret` | GitHub OAuth App Secret | `abc123...` |
| `google.client.id` | Google OAuth Client ID | `123456.apps.googleusercontent.com` |
| `google.client.secret` | Google OAuth Client Secret | `GOCSPX-...` |
| `db.url` | Full JDBC MySQL connection string | `jdbc:mysql://localhost:3306/smart_study_planner` |
| `db.user` | MySQL username | `root` |
| `db.password` | MySQL password | `yourpassword` |
| `ai.api.key` | Groq API key | `gsk_...` |
| `ai.model` | LLM model to use for generation | `llama-3.3-70b-versatile` |
| `ai.max.tokens` | Max tokens per AI response | `2000` |
| `app.debug` | Enable verbose debug logging | `true` / `false` |

---

## 🧠 AI Plan Generation

Smart Study Planner uses **Groq's free API** with **LLaMA 3.3 70B Versatile** for near-instant AI inference.

**For IT Students**, the flow works like this:

1. User selects a GitHub repository from their account
2. `ProjectTypeDetector` analyses the repo to identify the project category
3. `AIPlanService` sends a structured prompt to Groq containing: repository name, project type, experience level, daily hours, and target features
4. The model returns a structured breakdown of tasks, milestones, and recommended commit targets
5. Tasks are saved to `study_tasks`, topics to `topics`, and the parent plan to `goals`

**For Normal Students**, the flow is:

1. User adds subjects and topics with difficulty and size values
2. `TopicWeightCalculator` computes a scheduling weight per topic using `difficulty × size`
3. `NormalPlanGenerator` distributes sessions across all available days using the 30 / 40 / 30 split (Learn / Practice / Review)
4. Tasks are written to `study_tasks` with each day's session spread evenly up to and including the deadline

---

## 📊 Analytics Dashboard (IT Students)

`GitHubProgressFrame` renders three live charts using **JFreeChart**:

- **Pie Chart** — Snapshot of task status distribution across the active plan
- **Line Chart** — Day-by-day progress trend for the last 7 days
- **Bar Chart** — Weekly summary comparing Completed, Missed, and Pending tasks side by side

All chart data is queried live from `study_tasks` filtered by the current user and active plan.

**CSV Export** lets IT students save their full task history to a `.csv` file using a native file chooser dialog. The exported file can be opened directly in Excel or Google Sheets.

---

## 🔒 Security Notes

- OAuth tokens are stored in the `users` table server-side and are never surfaced in the UI
- `config.properties` is excluded via `.gitignore` — use environment variables for production deployments
- MySQL is configured with `useSSL=false` for local development only; SSL should be enabled in any production environment
- No plaintext passwords are stored — authentication is handled exclusively through Google and GitHub OAuth providers

---

## 🛣️ Roadmap

- [ ] Built-in Pomodoro timer per task session
- [ ] Desktop tray notifications for daily study reminders
- [ ] Weekly progress report export to PDF
- [ ] Dark mode support across all Swing frames
- [ ] Google Calendar sync for plan deadlines
- [ ] GitHub Actions webhook for real-time commit sync
- [ ] Multi-language UI support (Hindi, Marathi)

---

## 🤝 Contributing

Contributions of all kinds are welcome!

```bash
# Fork the repository, then:
git checkout -b feature/your-feature-name
git commit -m "feat: describe your change clearly"
git push origin feature/your-feature-name
# Open a Pull Request targeting the abhi7 branch
```

Please use conventional commit prefixes: `feat:`, `fix:`, `docs:`, `refactor:`, `test:`

---

## 🐛 Known Issues

- GitHub commit sync may be delayed up to 15 minutes after first login
- AI plan generation for durations shorter than 7 days may produce overlapping tasks
- JFreeChart charts may flicker briefly on first render on lower-end systems
- Google OAuth requires an active internet connection even in local development mode

---
