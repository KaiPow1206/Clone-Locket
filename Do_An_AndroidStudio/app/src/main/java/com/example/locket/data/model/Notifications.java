package com.example.locket.data.model;

public class Notifications {
    public static class Notification {
        private String type;
        private String createdAt;

        private boolean markRead;
        private NotificationSender sender;

        public Notification(String type,boolean markRead, String createdAt, NotificationSender sender) {
            this.type = type;
            this.createdAt = createdAt;
            this.sender = sender;
            this.markRead = markRead;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(String createdAt) {
            this.createdAt = createdAt;
        }

        public NotificationSender getSender() {
            return sender;
        }

        public void setSender(NotificationSender sender) {
            this.sender = sender;
        }
        public boolean isMarkRead() {
            return markRead;
        }

        public void setMarkRead(boolean markRead) {
            this.markRead = markRead;
        }
    }

    public static class NotificationSender {
        private String username;
        private String avatar_url;

        public NotificationSender(String username, String avatar_url) {
            this.username = username;
            this.avatar_url = avatar_url;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getAvatar_url() {
            return avatar_url;
        }

        public void setAvatar_url(String avatar_url) {
            this.avatar_url = avatar_url;
        }
    }
}
