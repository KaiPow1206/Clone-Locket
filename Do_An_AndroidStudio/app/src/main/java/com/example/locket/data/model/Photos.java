package com.example.locket.data.model;

public class Photos {
    public class Photo {
        private int id;
        private String image_url;
        private String caption;
        private String createdAt;
        private PhotoUser user;

        public Photo(int id, String image_url, String caption, String createdAt, PhotoUser user) {
            this.id = id;
            this.image_url = image_url;
            this.caption = caption;
            this.createdAt = createdAt;
            this.user = user;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getImage_url() {
            return image_url;
        }

        public void setImage_url(String image_url) {
            this.image_url = image_url;
        }

        public String getCaption() {
            return caption;
        }

        public void setCaption(String caption) {
            this.caption = caption;
        }

        public String getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(String createdAt) {
            this.createdAt = createdAt;
        }

        public PhotoUser getUser() {
            return user;
        }

        public void setUser(PhotoUser user) {
            this.user = user;
        }
    }

    public class PhotoUser {
        private String username;
        private String avatar_url;

        public PhotoUser(String username, String avatar_url) {
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

    public class PostPhotoRequest {
        private String image_url;
        private String caption;

        public PostPhotoRequest(String image_url, String caption) {
            this.image_url = image_url;
            this.caption = caption;
        }

        public String getImage_url() {
            return image_url;
        }

        public void setImage_url(String image_url) {
            this.image_url = image_url;
        }

        public String getCaption() {
            return caption;
        }

        public void setCaption(String caption) {
            this.caption = caption;
        }
    }

}
