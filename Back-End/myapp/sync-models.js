import { exec } from 'child_process';
import fs from 'fs/promises';
import path from 'path';

async function clearModels() {
  const modelsDir = path.resolve('src/models');
  try {
    const files = await fs.readdir(modelsDir);
    await Promise.all(files.map(file => fs.unlink(path.join(modelsDir, file))));
    console.log('Đã xóa model cũ');
  } catch (err) {
    console.error('Lỗi khi xóa model:', err);
  }
}

async function generateModels() {
  return new Promise((resolve, reject) => {
    exec(
      `npx sequelize-auto -h localhost -d locket -u root -x 123456 -p 3306 --dialect mysql -o src/models -l esm`,
      (error, stdout, stderr) => {
        if (error) {
          console.error(`Lỗi khi chạy sequelize-auto: ${error.message}`);
          return reject(error);
        }
        if (stderr) console.error(stderr);
        console.log('Sinh model mới xong');
        resolve();
      }
    );
  });
}

async function main() {
  await clearModels();
  await generateModels();
  console.log('Hoàn thành đồng bộ model với database');
}

main().catch(console.error);
