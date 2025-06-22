import _sequelize from "sequelize";
const DataTypes = _sequelize.DataTypes;
import _friends from  "./friends.js";
import _notifications from  "./notifications.js";
import _photo_reactions from  "./photo_reactions.js";
import _photos from  "./photos.js";
import _users from  "./users.js";

export default function initModels(sequelize) {
  const friends = _friends.init(sequelize, DataTypes);
  const notifications = _notifications.init(sequelize, DataTypes);
  const photo_reactions = _photo_reactions.init(sequelize, DataTypes);
  const photos = _photos.init(sequelize, DataTypes);
  const users = _users.init(sequelize, DataTypes);

  photo_reactions.belongsTo(photos, { as: "photo", foreignKey: "photo_id"});
  photos.hasMany(photo_reactions, { as: "photo_reactions", foreignKey: "photo_id"});
  friends.belongsTo(users, { as: "user", foreignKey: "user_id"});
  users.hasMany(friends, { as: "friends", foreignKey: "user_id"});
  friends.belongsTo(users, { as: "friend", foreignKey: "friend_id"});
  users.hasMany(friends, { as: "friend_friends", foreignKey: "friend_id"});
  notifications.belongsTo(users, { as: "user", foreignKey: "user_id"});
  users.hasMany(notifications, { as: "notifications", foreignKey: "user_id"});
  notifications.belongsTo(users, { as: "sender", foreignKey: "sender_id"});
  users.hasMany(notifications, { as: "sender_notifications", foreignKey: "sender_id"});
  photo_reactions.belongsTo(users, { as: "user", foreignKey: "user_id"});
  users.hasMany(photo_reactions, { as: "photo_reactions", foreignKey: "user_id"});
  photos.belongsTo(users, { as: "user", foreignKey: "user_id"});
  users.hasMany(photos, { as: "photos", foreignKey: "user_id"});

  return {
    friends,
    notifications,
    photo_reactions,
    photos,
    users,
  };
}
