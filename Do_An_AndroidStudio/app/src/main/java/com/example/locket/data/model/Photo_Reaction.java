package com.example.locket.data.model;

public class Photo_Reaction {
    public static class ReactionRequest {
        private int photoId;
        private String reactionType;

        public ReactionRequest(int photoId, String reactionType) {
            this.photoId = photoId;
            this.reactionType = reactionType;
        }

        public int getPhotoId() {
            return photoId;
        }

        public void setPhotoId(int photoId) {
            this.photoId = photoId;
        }

        public String getReactionType() {
            return reactionType;
        }

        public void setReactionType(String reactionType) {
            this.reactionType = reactionType;
        }
    }

    public static class DeleteReactionRequest {
        private int photoId;
        private String type;

        public DeleteReactionRequest(int photoId, String reactionType) {
            this.photoId = photoId;
            this.type = reactionType;
        }

        public int getPhotoId() {
            return photoId;
        }

        public void setPhotoId(int photoId) {
            this.photoId = photoId;
        }

        public String getReactionType() {
            return type;
        }

        public void setReactionType(String reactionType) {
            this.type = reactionType;
        }
    }

    public class ReactionSummary {
        private int love;
        private int haha;
        private int wow;
        private int sad;

        public ReactionSummary(int like, int love, int haha, int wow, int sad) {
            this.love = love;
            this.haha = haha;
            this.wow = wow;
            this.sad = sad;
        }



        public int getLove() {
            return love;
        }

        public void setLove(int love) {
            this.love = love;
        }

        public int getHaha() {
            return haha;
        }

        public void setHaha(int haha) {
            this.haha = haha;
        }

        public int getWow() {
            return wow;
        }

        public void setWow(int wow) {
            this.wow = wow;
        }

        public int getSad() {
            return sad;
        }

        public void setSad(int sad) {
            this.sad = sad;
        }
    }
}
