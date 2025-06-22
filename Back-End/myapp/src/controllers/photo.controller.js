import sequelize from '../config/connect.js';
import initModels from "../models/init-models.js";
import { Op } from 'sequelize';
import dotenv from 'dotenv';
import path from 'path';
dotenv.config();

const model = initModels(sequelize);

const getPhotos = async (req, res) => {
  try {
    const userId = req.user?.id;
    if (!userId) {
      return res.status(401).json({ message: "Unauthorized" });
    }

    // Tìm bạn bè đã accept
    const friendships = await model.friends.findAll({
      where: {
        status: 'accepted',
        [Op.or]: [
          { user_id: userId },
          { friend_id: userId }
        ]
      }
    });

    const friendIds = friendships.map(f =>
      f.user_id === userId ? f.friend_id : f.user_id
    );
    friendIds.push(userId); // thêm bản thân vào

    const uniqueFriendIds = [...new Set(friendIds)];

    // Truy vấn tất cả ảnh của bản thân + bạn bè
    const photos = await model.photos.findAll({
      where: {
        user_id: {
          [Op.in]: uniqueFriendIds
        }
      },
      attributes: ['id', 'image_url', 'caption', 'createdAt'],
      include: [{
        model: model.users,
        as: 'user',
        attributes: ['username', 'avatar_url']
      }],
      order: [['createdAt', 'DESC']]
    });
    if (photos.length === 0) {
      return res.status(404).json({ message: "No photos found" });
    }
    
    res.status(200).json(photos);
  } catch (error) {
    console.error(error);
    res.status(500).json({ message: "Internal Server Error" });
  }
};


const postPhotos = async (req, res) => {
  try {
    const userId = req.user?.id;
    if (!userId) {
      return res.status(401).json({ message: "Unauthorized" });
    }

    const file = req.file;
    const caption = req.body.caption || '';

    if (!file) {
      return res.status(400).json({ message: 'No file uploaded' });
    }

    const host = process.env.HOST_URL || 'http://localhost:3000';
    const image_url = `${host}/uploads/${file.filename}`;

    const newPhoto = await model.photos.create({
      user_id: userId,
      image_url,
      caption
    });

    res.status(201).json(newPhoto);
  } catch (error) {
    console.error(error);
    res.status(500).json({ message: "Internal Server Error" });
  }
};


const deletePhotos = async (req, res) => {
   try {
      const userId = req.user?.id;
      if (!userId) {
         return res.status(401).json({ message: "Unauthorized" });
      }

      const { photoId } = req.params;

      const photo = await model.photos.findOne({
         where: {
            id: photoId,
            user_id: userId
         }
      });

      if (!photo) {
         return res.status(404).json({ message: "Photo not found" });
      }

      await photo.destroy();
      res.status(200).json({ message: "Photo deleted successfully" });
   } catch (error) {
      console.error(error);
      res.status(500).json({ message: "Internal Server Error" });
   }
};

export {
   getPhotos,
   postPhotos,
   deletePhotos
};
