import sequelize from '../config/connect.js';
import initModels from "../models/init-models.js";
import { Op } from 'sequelize';

const model = initModels(sequelize);

const addReaction = async (req, res) => {
   const userId = req.user?.id;
   if (!userId) {
      return res.status(401).json({ message: "Unauthorized" });
   }
   try {
      const { photoId, reactionType } = req.body;
      // Kiểm tra ảnh tồn tại
      const photo = await model.photos.findOne({
         where: { id: photoId }
      });

      if (!photo) {
         return res.status(404).json({ message: "Photo not found" });
      }

      // Kiểm tra đã từng thả reaction chưa
      const existingReaction = await model.photo_reactions.findOne({
         where: {
            photo_id: photoId,
            user_id: userId,
            type: reactionType
         }
      });

      if (existingReaction) {
         return res.status(400).json({ message: 'Reaction already exists for this photo.' });
      }

      // Thêm reaction
      const newReaction = await model.photo_reactions.create({
         photo_id: photoId,
         user_id: userId,
         type: reactionType
      });

      await model.notifications.create({
         user_id: photo.user_id,
         sender_id: userId,
         type: 'reaction'
      });
      return res.status(201).json(newReaction);
   } catch (error) {
      console.log(error);
      return res.status(500).json({ message: 'Internal server error' });
   }
};


const deleteReaction = async (req, res) => {
   const userId = req.user?.id;
   if (!userId) {
      return res.status(401).json({ message: "Unauthorized" });
   }

   const { photoId, type } = req.body;
   if (!photoId || !type) {
      return res.status(400).json({ message: "Photo ID and type reaction are required" });
   }

   try {
      const reaction = await model.photo_reactions.findOne({
         where: {
            photo_id: photoId,
            user_id: userId,
            type: type
         }
      });

      if (!reaction) {
         return res.status(404).json({ message: "Reaction not found" });
      }

      await reaction.destroy();
      return res.status(200).json({ message: "Reaction removed successfully" });
   } catch (error) {
      console.error(error);
      return res.status(500).json({ message: "Internal server error" });
   }
};

const getAllReactions = async (req, res) => {
   const userId = req.user?.id;
   if (!userId) {
      return res.status(401).json({ message: "Unauthorized" });
   }
   const { photoId } = req.query;
   if (!photoId) {
      return res.status(400).json({ message: "Photo ID is required" });
   }
   try {
      const checkPhotoID = await model.photos.findOne({
         where: { id: photoId }
      });
      if (!checkPhotoID) {
         return res.status(400).json({ message: "Photo not found" });
      }

      const reactionsCount = await model.photo_reactions.findAll({
         where: { photo_id: photoId },
         attributes: [
            'type',
            [sequelize.fn('COUNT', sequelize.col('type')), 'count']
         ],
         group: ['type']
      });

      const summary = {
         love: 0,
         haha: 0,
         wow: 0,
         sad: 0
      };

      reactionsCount.forEach(r => {
         summary[r.type] = parseInt(r.get('count'), 10);
      });

      return res.status(200).json(summary);
   } catch (error) {
      console.error(error);
      return res.status(500).json({ message: 'Internal server error' });
   }
};

export {
   addReaction,
   deleteReaction,
   getAllReactions
}