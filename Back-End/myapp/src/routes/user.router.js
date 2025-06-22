import express from 'express';
import { acptFriend, addFriend, deleteUser, getAllRequests, getProfile, getSentRequests, getUser, rejectFriend, searchUsers, unFriend, updateUser } from '../controllers/user.controller.js';
import { middlewareToken } from '../config/jwt.js';
import multer from 'multer';
import path from 'path';
const userRoutes = express.Router();

userRoutes.get(`/get-profile`, middlewareToken, getProfile);
userRoutes.get(`/get-users`,middlewareToken,getUser);
userRoutes.delete(`/delete-user`,middlewareToken, deleteUser);
const storage = multer.diskStorage({
   destination: (req, file, cb) => {
      cb(null, 'uploads/');
   },
   filename: (req, file, cb) => {
      cb(null, Date.now() + path.extname(file.originalname));
   }
});
const upload = multer({ storage });
userRoutes.put(`/update-user`,middlewareToken,upload.single('image'), updateUser);
userRoutes.get(`/get-sentrequests`, middlewareToken, getSentRequests);
userRoutes.get(`/get-allrequests`, middlewareToken, getAllRequests);
userRoutes.post("/add-friend", middlewareToken, addFriend);
userRoutes.post("/un-friend", middlewareToken, unFriend);
userRoutes.patch("/accept-friend", middlewareToken, acptFriend);
userRoutes.patch("/reject-friend", middlewareToken, rejectFriend);
userRoutes.get('/search-users',middlewareToken, searchUsers);

export default userRoutes;