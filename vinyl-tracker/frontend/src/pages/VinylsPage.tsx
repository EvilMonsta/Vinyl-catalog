import { Box, Typography, Button, Container, Skeleton } from '@mui/material';
import { Swiper, SwiperSlide } from 'swiper/react';
import 'swiper/css';
import 'swiper/css/navigation';
import { Navigation, Autoplay } from 'swiper/modules';
import { useQuery } from '@tanstack/react-query';
import axios from '../api/axios';
import VinylCard from '../components/VinylCard';
import Footer from '../components/Footer';
import VinylDetailsModal from '../components/VinylDetailsModal';
import { useMemo, useRef, useState } from 'react';
import { useNavigate } from 'react-router-dom';

export default function HomePage() {
  const navigate = useNavigate();
  const newRef = useRef<HTMLDivElement | null>(null);
  const randomRef = useRef<HTMLDivElement | null>(null);

  const { data: newVinyls, isLoading: loadingNew } = useQuery({
    queryKey: ['newVinyls2025'],
    queryFn: () =>
      axios.get('/api/vinyls/new?limit=10').then(res =>
        Array.isArray(res.data) ? res.data : res.data.vinyls || []
      ),
  });

  const { data: randomVinyls, isLoading: loadingRandom } = useQuery({
    queryKey: ['randomVinyls'],
    queryFn: () =>
      axios.get('/api/vinyls/random?limit=10').then(res =>
        Array.isArray(res.data) ? res.data : res.data.vinyls || []
      ),
  });

  const [selectedVinyl, setSelectedVinyl] = useState<any>(null);

  const heroBg = useMemo(() => {
    const cover = newVinyls?.[0]?.coverUrl as string | undefined;
    if (!cover) return undefined;
    return `linear-gradient(180deg, rgba(10,12,15,.85) 0%, rgba(10,12,15,.7) 35%, rgba(10,12,15,.9) 100%), url(${cover})`;
  }, [newVinyls]);

  const jumpTo = (ref: React.RefObject<HTMLDivElement>) =>
    ref.current?.scrollIntoView({ behavior: 'smooth', block: 'start' });

  const renderVinylSlides = (vinyls: any[], loading: boolean) => {
    if (loading) {
      return Array.from({ length: 6 }).map((_, idx) => (
        <SwiperSlide key={idx}>
          <Skeleton
            variant="rectangular"
            sx={{ width: 240, aspectRatio: '1/1', borderRadius: 2, bgcolor: '#242a31' }}
          />
        </SwiperSlide>
      ));
    }
    if (!Array.isArray(vinyls) || vinyls.length === 0) {
      return <Typography sx={{ color: 'text.secondary', mt: 2 }}>Нет доступных пластинок.</Typography>;
    }
    return vinyls.map(vinyl => (
      <SwiperSlide key={vinyl.id}>
        <VinylCard vinyl={vinyl} onOpenDetails={() => setSelectedVinyl(vinyl)} />
      </SwiperSlide>
    ));
  };

  return (
    <>
      {/* HERO — без поиска и жанров */}
      <Box
        sx={{
          position: 'relative',
          minHeight: { xs: 320, md: 440 },
          display: 'flex',
          alignItems: 'center',
          overflow: 'hidden',
          borderBottom: '1px solid',
          borderColor: 'rgba(255,255,255,0.06)',
          background: heroBg ||
            'radial-gradient(1200px 500px at 10% -20%, rgba(110,231,255,.15) 0%, rgba(110,231,255,0) 60%), radial-gradient(1000px 500px at 90% -10%, rgba(124,241,82,.15) 0%, rgba(124,241,82,0) 60%), linear-gradient(180deg,#0b0f13 0%, #0e1216 100%)',
          backgroundSize: heroBg ? 'cover' : 'auto',
          backgroundPosition: 'center',
        }}
      >
        <Box
          sx={{
            position: 'absolute',
            inset: 0,
            background: 'radial-gradient(60% 60% at 80% 0%, rgba(0,0,0,.15) 0%, rgba(0,0,0,.6) 65%, rgba(0,0,0,.85) 100%)',
          }}
        />
        <Container maxWidth="xl" sx={{ position: 'relative', zIndex: 1 }}>
          <Box sx={{ maxWidth: 820 }}>
            <Typography variant="h2" sx={{ fontSize: { xs: 32, md: 44 }, fontWeight: 800, lineHeight: 1.1 }}>
              Открой для себя<span style={{ color: 'var(--mui-palette-primary-main)', marginLeft: 8 }}>винил</span>
            </Typography>
            <Typography sx={{ mt: 1.5, color: 'text.secondary', maxWidth: 700, fontSize: { xs: 14, md: 16 } }}>
              Новый релиз? Культовая классика? Собери свою коллекцию и следи за любимыми артистами — в одном месте.
            </Typography>

            <Box sx={{ mt: 3, display: 'flex', gap: 1.5, flexWrap: 'wrap' }}>
              <Button variant="outlined" onClick={() => jumpTo(newRef)}>Новые релизы</Button>
              <Button variant="text" onClick={() => jumpTo(randomRef)}>Случайные</Button>
              <Button variant="text" onClick={() => navigate('/catalog')}>Открыть каталог →</Button>
            </Box>
          </Box>
        </Container>
      </Box>

      {/* Контент */}
      <Container maxWidth="xl" sx={{ mt: 4, pb: 6 }}>
        <Box ref={newRef} sx={{ display: 'flex', alignItems: 'baseline', justifyContent: 'space-between', mb: 2 }}>
          <Typography variant="h5">Новые пластинки 2025</Typography>
          <Button size="small" variant="text" href="/catalog?year=2025" sx={{ color: 'primary.main' }}>
            Смотреть всё →
          </Button>
        </Box>

        <Swiper
          modules={[Navigation, Autoplay]}
          navigation
          autoplay={{ delay: 5000, disableOnInteraction: false, pauseOnMouseEnter: true }}
          spaceBetween={16}
          loop
          style={{ padding: '8px 4px 12px' }}
          breakpoints={{ 320: { slidesPerView: 2 }, 600: { slidesPerView: 3 }, 900: { slidesPerView: 4 }, 1200: { slidesPerView: 6 } }}
        >
          {renderVinylSlides(newVinyls || [], loadingNew)}
        </Swiper>

        <Box ref={randomRef} sx={{ display: 'flex', alignItems: 'baseline', justifyContent: 'space-between', mt: 6, mb: 2 }}>
          <Typography variant="h5">Случайные пластинки</Typography>
          <Button size="small" variant="text" href="/catalog" sx={{ color: 'primary.main' }}>
            К каталогу →
          </Button>
        </Box>

        <Swiper
          modules={[Navigation, Autoplay]}
          navigation
          autoplay={{ delay: 6000, disableOnInteraction: false, pauseOnMouseEnter: true }}
          spaceBetween={16}
          loop
          style={{ padding: '8px 4px 12px' }}
          breakpoints={{ 320: { slidesPerView: 2 }, 600: { slidesPerView: 3 }, 900: { slidesPerView: 4 }, 1200: { slidesPerView: 6 } }}
        >
          {renderVinylSlides(randomVinyls || [], loadingRandom)}
        </Swiper>

        <Box
          sx={{
            mt: 8,
            p: 4,
            textAlign: 'center',
            background: 'linear-gradient(180deg,#14181d 0%, #111418 100%)',
            borderRadius: 3,
            boxShadow: '0 6px 22px rgba(0,0,0,.35)',
          }}
        >
          <Typography variant="h5" gutterBottom>Больше возможностей с подпиской</Typography>
          <Typography variant="body2" sx={{ mb: 2, color: 'text.secondary' }}>
            Расширенный доступ к коллекциям, персональные рекомендации и другое.
          </Typography>
          <Button variant="contained" color="primary" href="/subscription">Подробнее</Button>
        </Box>
      </Container>

      <VinylDetailsModal open={!!selectedVinyl} onClose={() => setSelectedVinyl(null)} vinyl={selectedVinyl} />
      <Footer />
    </>
  );
}
