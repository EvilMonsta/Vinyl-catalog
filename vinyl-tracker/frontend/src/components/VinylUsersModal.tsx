import {
  Dialog, DialogTitle, DialogContent, DialogActions,
  Button, Table, TableHead, TableRow, TableCell, TableBody, Chip
} from '@mui/material';
import { useEffect, useState } from 'react';
import axios from '../api/axios';
import { useAuth } from '../context/AuthContext';

interface UserVinyl {
  user: {
    id: number;
    username: string;
    email: string;
  };
  statusId: number;
}


const statusMap: Record<number, string> = {
  1: 'Хочу',
  2: 'Имею',
  3: 'В пути'
};

export default function VinylUsersModal({ vinylId, open, onClose }: { vinylId: number, open: boolean, onClose: () => void }) {
  const [users, setUsers] = useState<UserVinyl[]>([]);
  const auth = useAuth();

  useEffect(() => {
    if (open) {
      axios.get(`/api/admin/user-vinyls/getUsers/${vinylId}`, {
        headers: { Authorization: `Bearer ${auth?.user?.token}` }
      }).then(res => setUsers(res.data))
        .catch(err => console.error('Ошибка загрузки пользователей:', err));
    }
  }, [open, vinylId, auth]);

  return (
    <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth>
      <DialogTitle>Кто добавил эту пластинку</DialogTitle>
      <DialogContent>
        {users.length === 0 ? (
          <p>Никто не добавил эту пластинку.</p>
        ) : (
          <Table size="small">
            <TableHead>
              <TableRow>
                <TableCell>Username</TableCell>
                <TableCell>Email</TableCell>
                <TableCell>Статус</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {users.map(record => (
                <TableRow key={record.user.id}>
                  <TableCell>{record.user.username}</TableCell>
                  <TableCell>{record.user.email}</TableCell>
                  <TableCell>
                    <Chip label={statusMap[record.statusId] || `Статус #${record.statusId}`} />
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        )}
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose}>Закрыть</Button>
      </DialogActions>
    </Dialog>
  );
}
