package com.example.locket.data.model;

import java.util.List;

public class Users {
    public static class User {
        private String username;

        private String email;
        private String avatar_url;
        private List<SearchUserResponse> friends;


        public User(String username, String email, String avatar_url,List<SearchUserResponse>  friends) {
            this.username = username;
            this.email = email;
            this.avatar_url = avatar_url;
            this.friends = friends;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getAvatar_url() {
            return avatar_url;
        }

        public void setAvatar_url(String avatar_url) {
            this.avatar_url = avatar_url;
        }

        public List<SearchUserResponse> getFriends() {
            return friends;
        }

        public void setFriends(List<SearchUserResponse> friends) {
            this.friends = friends;
        }
    }

    public static class UpdateUserRequest {
        private String email;
        private String password;
        private String avatar_url;

        public UpdateUserRequest(String email, String password, String avatar_url) {
            this.email = email;
            this.password = password;
            this.avatar_url = avatar_url;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getAvatar_url() {
            return avatar_url;
        }

        public void setAvatar_url(String avatar_url) {
            this.avatar_url = avatar_url;
        }
    }

    public static class FriendRequest {
        private String username;
        private String friendUsername;
        private String status;

        public FriendRequest(String friendUsername) {
            this.friendUsername = friendUsername;
        }


        public String getUsername() {
            return username;
        }

        public String getStatus() {
            return status;
        }
    }


    public static class SearchUserResponse {
        private String username;
        private String avatar_url;
        private String friendshipStatus; // "friends", "request_sent", "none"

        public SearchUserResponse(String username, String avatar_url, String friendshipStatus) {
            this.username = username;
            this.avatar_url = avatar_url;
            this.friendshipStatus = friendshipStatus;
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

        public String getFriendshipStatus() {
            return friendshipStatus;
        }

        public void setFriendshipStatus(String friendshipStatus) {
            this.friendshipStatus = friendshipStatus;
        }
    }
}
