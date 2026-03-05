-- Create database
CREATE DATABASE IF NOT EXISTS smart_study_planner;
USE smart_study_planner;

-- Users table
CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    role ENUM('NORMAL', 'IT') DEFAULT 'NORMAL',
    oauth_provider ENUM('GOOGLE', 'GITHUB') NOT NULL,
    github_username VARCHAR(255),
    access_token TEXT,
    avatar_url TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_email (email),
    INDEX idx_role (role)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Goals/Study Plans table
CREATE TABLE IF NOT EXISTS goals (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    repository_name VARCHAR(255),
    deadline DATE NOT NULL,
    difficulty ENUM('EASY', 'MODERATE', 'HARD') DEFAULT 'MODERATE',
    daily_hours INT DEFAULT 2,
    completion_percentage INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_deadline (deadline)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Study tasks table
CREATE TABLE IF NOT EXISTS study_tasks (
    id INT AUTO_INCREMENT PRIMARY KEY,
    goal_id INT NOT NULL,
    task_date DATE NOT NULL,
    description TEXT NOT NULL,
    required_commit BOOLEAN DEFAULT FALSE,
    status ENUM('PENDING', 'COMPLETED', 'MISSED') DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (goal_id) REFERENCES goals(id) ON DELETE CASCADE,
    INDEX idx_goal_id (goal_id),
    INDEX idx_task_date (task_date),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- GitHub activity tracking
CREATE TABLE IF NOT EXISTS github_activity (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    repo_name VARCHAR(255) NOT NULL,
    commit_count INT DEFAULT 0,
    last_commit_date DATE,
    streak_count INT DEFAULT 0,
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY unique_user_repo (user_id, repo_name),
    INDEX idx_user_id (user_id),
    INDEX idx_last_commit (last_commit_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Configuration table
CREATE TABLE IF NOT EXISTS app_config (
    id INT AUTO_INCREMENT PRIMARY KEY,
    config_key VARCHAR(100) UNIQUE NOT NULL,
    config_value TEXT,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Insert default config values
INSERT INTO app_config (config_key, config_value) VALUES
('app_version', '1.0.0'),
('app_name', 'Smart Study Planner'),
('last_update_check', CURRENT_TIMESTAMP)
ON DUPLICATE KEY UPDATE config_value = VALUES(config_value);