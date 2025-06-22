import express from 'express';
import { allNotifications, readNotifications} from '../controllers/noti.controller.js';
import { middlewareToken } from '../config/jwt.js';
const notiRouter = express.Router();


notiRouter.get("/all-noti",middlewareToken,allNotifications);
notiRouter.put("/read-noti",middlewareToken,readNotifications);

export default notiRouter;