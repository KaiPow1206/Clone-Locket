import sequelize from '../config/connect.js';
import initModels from "../models/init-models.js";
import { Op } from 'sequelize';

const model = initModels(sequelize);

const allNotifications = async (req, res) => {
   try {

      const userId = req.user?.id;
      if (!userId) {
         return res.status(401).json({ message: "Unauthorized" });
      }
      const notifications = await model.notifications.findAll({
         where: {
            user_id: userId,
            mark_read: false,
            sender_id: { [Op.ne]: userId }
         },
         order: [['createdAt', 'DESC']],
         include: [
            {
               model: model.users,
               as: 'sender',
               attributes: ['username', 'avatar_url']
            }
         ]
      });
      if (notifications.length === 0) {
         return res.status(200).json([]);
      }
      res.status(200).json(notifications);
   } catch (error) {
      console.error(error);
      res.status(500).json({ message: "Internal Server Error" });
   }
};

const readNotifications = async (req, res) => {
   try {
      const userId = req.user?.id;
      if (!userId) {
         return res.status(401).json({ message: "Unauthorized" });
      }

      await model.notifications.update(
         { mark_read: true },
         { where: { user_id: userId, mark_read: false } }
      );

      res.status(200).json({ message: "All notifications marked as read" });
   } catch (error) {
      console.error(error);
      res.status(500).json({ message: "Internal Server Error" });
   }
};


export {
   allNotifications,
   readNotifications
};