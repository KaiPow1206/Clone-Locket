import express from 'express';
import { addReaction, deleteReaction, getAllReactions } from '../controllers/photo_reaction.controller.js';
import { middlewareToken } from '../config/jwt.js';


const photoReactionRoutes = express.Router();
photoReactionRoutes.get("/all-reactions",middlewareToken,getAllReactions);
photoReactionRoutes.post("/add-reaction", middlewareToken,addReaction);
photoReactionRoutes.delete("/delete-reaction",middlewareToken, deleteReaction);

export default photoReactionRoutes;