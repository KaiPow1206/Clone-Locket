import express from 'express';
import { deletePhotos, getPhotos, postPhotos } from '../controllers/photo.controller.js';
import { middlewareToken } from '../config/jwt.js';
import multer from 'multer';
import path from 'path';
const photoRoutes = express.Router();

photoRoutes.get('/get-photos',middlewareToken, getPhotos);
const storage = multer.diskStorage({
   destination: (req, file, cb) => {
      cb(null, 'uploads/');
   },
   filename: (req, file, cb) => {
      cb(null, Date.now() + path.extname(file.originalname));
   }
});
const upload = multer({ storage });
photoRoutes.post('/post-photos', middlewareToken,upload.single('image'), postPhotos);
photoRoutes.delete('/delete-photo/:photoId', middlewareToken, deletePhotos);
export default photoRoutes;