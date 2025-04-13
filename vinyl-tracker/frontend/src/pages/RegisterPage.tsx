import { useMutation } from '@tanstack/react-query';
import { useForm } from 'react-hook-form';
import { RegisterRequest, registerUser } from '../api/auth';
import {useEffect, useState} from 'react';
import { TextField, Button, Typography, Box } from '@mui/material';
import { useNavigate } from 'react-router-dom';

export default function RegisterPage() {
  const { register, handleSubmit, formState: { errors }, reset } = useForm<RegisterRequest>();
  const [serverError, setServerError] = useState('');
  const navigate = useNavigate();

  useEffect(() => {
    reset({
      username: '',
      email: '',
      password: ''
    });
  }, []);

  const mutation = useMutation({
    mutationFn: registerUser,
    onSuccess: () => {
      navigate('/login');
    },
    onError: (err: any) => {
      const raw = err?.response?.data;
      const errorText = typeof raw === 'string' ? raw : raw?.error;

      if (err?.response?.status === 409) {
        if (errorText?.toLowerCase().includes('email')) {
          setServerError('Пользователь с таким email уже зарегистрирован');
        } else if (errorText?.toLowerCase().includes('username')) {
          setServerError('Пользователь с таким именем уже существует');
        } else {
          setServerError('Такой пользователь уже существует');
        }
      } else if (err?.response?.status === 400) {
        setServerError('Не все поля заполнены корректно');
      } else {
        setServerError('Что-то пошло не так. Попробуй ещё раз');
      }

      console.error('❌ Ошибка регистрации:', err);
    }


  });

  const onSubmit = (data: RegisterRequest) => {
    setServerError('');
    if (!data.username || !data.email || !data.password) {
      setServerError('Пожалуйста, заполните все поля');
      return;
    }

    mutation.mutate({ ...data, roleId: 1 });
  };

  return (
    <Box sx={{ maxWidth: 400, mx: 'auto', mt: 5 }}>
      <Typography variant="h5" gutterBottom>Регистрация</Typography>

      {serverError && (
        <Typography color="error" sx={{ mb: 2 }}>
          {serverError}
        </Typography>
      )}

      <form onSubmit={handleSubmit(onSubmit)}>
        <TextField
          fullWidth
          label="Имя пользователя"
          margin="normal"
          {...register('username', { required: true })}
          error={!!errors.username}
          helperText={errors.username && 'Введите имя'}
        />

        <TextField
          fullWidth
          label="Email"
          margin="normal"
          type="email"
          {...register('email', { required: true })}
          error={!!errors.email}
          helperText={errors.email && 'Введите email'}
        />

        <TextField
          fullWidth
          label="Пароль"
          margin="normal"
          type="password"
          {...register('password', { required: true, minLength: 8 })}
          error={!!errors.password}
          helperText={errors.password && 'Минимум 8 символов'}
        />

        <Button
          type="submit"
          fullWidth
          variant="contained"
          color="primary"
          sx={{ mt: 2 }}
        >
          Зарегистрироваться
        </Button>
      </form>
    </Box>
  );
}
