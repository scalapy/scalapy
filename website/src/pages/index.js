import React from 'react';
import clsx from 'clsx';
import Layout from '@theme/Layout';
import Link from '@docusaurus/Link';
import useDocusaurusContext from '@docusaurus/useDocusaurusContext';
import useBaseUrl from '@docusaurus/useBaseUrl';
import styles from './styles.module.css';
import useWindowSize, { windowSizes } from '@theme/hooks/useWindowSize';

const features = [
  {
    title: "Complete Ecosystem",
    description:
      "Use any Python library you can dream of. Want to train neural networks on GPUs with TensorFlow? ScalaPy supports it.",
    imageUrl: "img/tf-example.png",
    imageAlign: "right"
  },
  {
    title: "Strong Typing",
    description:
      "Add type definitions for Python libraries as you go to catch bugs before they happen in production.",
    imageUrl: "img/type-mismatch.png",
    imageAlign: "left"
  },
  {
    title: "Performant Interop",
    description:
      "Compile to native binaries with Scala Native to unlock maximum performance with direct bindings to CPython.",
    imageUrl: "img/native-time.png",
    imageAlign: "right"
  }
];

function Feature({imageUrl, imageAlign, title, description}) {
  const windowSize = useWindowSize();

  const imgUrl = useBaseUrl(imageUrl);
  return ((imageAlign == "right" || windowSize == windowSizes.mobile) ? <>
    <div className={clsx('col col--4', styles.feature)}>
      <h2>{title}</h2>
      <p>{description}</p>
    </div>
    <div className={clsx('col col--8', styles.feature)}>
      {imgUrl && (
        <div className="text--center">
          <img className={styles.featureImage} src={imgUrl} alt={title} />
        </div>
      )}
    </div>
  </> : <>
    <div className={clsx('col col--8', styles.feature)}>
      {imgUrl && (
        <div className="text--center">
          <img className={styles.featureImage} src={imgUrl} alt={title} />
        </div>
      )}
    </div>
    <div className={clsx('col col--4', styles.feature)}>
      <h2>{title}</h2>
      <p>{description}</p>
    </div>
  </>);
}

function Home() {
  const context = useDocusaurusContext();
  const {siteConfig = {}} = context;

  const wideLogoUrl = useBaseUrl("img/wide-logo.svg");

  return (
    <Layout
      title={`${siteConfig.title} - ${siteConfig.tagline}`}
      description={"ScalaPy makes it easy to use your favorite Python libraries from the comfort of Scala. With opt-in static typing and support for compilation to native binaries, ScalaPy scales from experimentation in notebooks to production environments."}>
      <header className={clsx('hero hero--primary', styles.heroBanner)}>
        <div className="container">
          <img style={{ width: "80%" }} src={wideLogoUrl}/>
          <p className="hero__subtitle" style={{ color: "white" }}>{siteConfig.tagline}</p>
          <div className={styles.buttons}>
            <Link
              className={clsx(
                'button button--outline button--secondary button--lg',
                styles.getStarted,
              )}
              style={{
                color: "white"
              }}
              to={useBaseUrl('docs/')}>
              Get Started
            </Link>
          </div>
        </div>
      </header>
      <main>
        {features && features.length > 0 && (
          <section className={styles.features}>
            <div className="container">
              {features.map((props, idx) => (
                <div className="row" style={{ paddingTop: 20 }}>
                  <Feature key={idx} {...props} />
                </div>
              ))}
            </div>
          </section>
        )}
      </main>
    </Layout>
  );
}

export default Home;
