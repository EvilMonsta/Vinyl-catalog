import {
  Table, TableBody, TableCell, TableContainer, TableHead, TableRow,
  Paper, Select, MenuItem, Typography, Dialog, DialogActions,
  DialogContent, DialogContentText, DialogTitle, Button
} from '@mui/material';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import axios from 'axios';
import { useState } from 'react';
import { useAuth } from '../../context/AuthContext';

export default function AdminUserTable() {
  const [selectedUserId, setSelectedUserId] = useState<number | null>(null);
  const [newRole, setNewRole] = useState('');
  const [confirmOpen, setConfirmOpen] = useState(false);
  const auth = useAuth();

  const queryClient = useQueryClient();

  const { data: users = [], isLoading } = useQuery({
    queryKey: ['adminUsers'],
    queryFn: () =>
      axios.get('/api/admin/users', {
        headers: {
          Authorization: `Bearer ${auth?.user?.token}`,
        },
      }).then(res => res.data),
    enabled: !!auth?.user, // <-- не запускаем до загрузки юзера
  });


  const mutation = useMutation({
    mutationFn: (updatedUser: any) =>
      axios.put(`/api/admin/users/update/${updatedUser.id}`, updatedUser),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['adminUsers'] }),
  });

  const handleChangeRole = (id: number, currentRole: string, selected: string) => {
    if (currentRole !== selected) {
      setSelectedUserId(id);
      setNewRole(selected);
      setConfirmOpen(true);
    }
  };

  const handleConfirm = () => {
    const user = users.find((u: any) => u.id === selectedUserId);
    if (user) {
      mutation.mutate({ ...user, role: newRole });
    }
    setConfirmOpen(false);
  };

  if (isLoading) return <Typography>Загрузка...</Typography>;

  return (
    <>
      <Typography variant="h4" gutterBottom color="primary">
        Панель администратора
      </Typography>
      <Typography variant="h5" sx={{ mb: 2 }}>Пользователи</Typography>
      <TableContainer component={Paper} sx={{ backgroundColor: '#1f1f1f' }}>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell>Имя</TableCell>
              <TableCell>Email</TableCell>
              <TableCell>Роль</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {users.map((user: any) => (
              <TableRow key={user.id}>
                <TableCell>{user.username}</TableCell>
                <TableCell>{user.email}</TableCell>
                <TableCell>
                  <Select
                    value={user.role}
                    onChange={(e) => handleChangeRole(user.id, user.role, e.target.value)}
                    sx={{
                      color: '#ccc',
                      backgroundColor: '#2a2a2a',
                      fontSize: '0.85rem',
                      width: 140
                    }}
                  >
                    <MenuItem value="USER">USER</MenuItem>
                    <MenuItem value="VIP_USER">VIP_USER</MenuItem>
                    <MenuItem value="ADMIN">ADMIN</MenuItem>
                  </Select>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </TableContainer>

      <Dialog open={confirmOpen} onClose={() => setConfirmOpen(false)}>
        <DialogTitle>Подтверждение</DialogTitle>
        <DialogContent>
          <DialogContentText>
            Вы уверены, что хотите изменить роль пользователя на <strong>{newRole}</strong>?
          </DialogContentText>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setConfirmOpen(false)}>Отмена</Button>
          <Button onClick={handleConfirm} variant="contained" color="success">
            Подтвердить
          </Button>
        </DialogActions>
      </Dialog>
    </>
  );
}
