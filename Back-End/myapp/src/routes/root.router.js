import express from 'express';
import userRoutes from './user.router.js';
import authRouter from './auth.router.js';
import photoRoutes from './photo.router.js';
import photoReactionRoutes from './photo_reaction.router.js';
import notiRouter from './noti.router.js';
const rootRoutes = express.Router();

rootRoutes.use("/users",userRoutes);
rootRoutes.use("/auth",authRouter);
rootRoutes.use("/photo", photoRoutes);
rootRoutes.use("/photo-reaction", photoReactionRoutes);
rootRoutes.use("/noti", notiRouter)

export default rootRoutes;